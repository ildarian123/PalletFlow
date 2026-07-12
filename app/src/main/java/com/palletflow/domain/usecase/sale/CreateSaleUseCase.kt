package com.palletflow.domain.usecase.sale

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.SaleExpenseDao
import com.palletflow.data.local.dao.SaleItemDao
import com.palletflow.data.local.entity.*
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.StockRepository
import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class CreateSaleUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val stockRepository: StockRepository,
    private val lotRepository: LotRepository,
    private val saleItemDao: SaleItemDao,
    private val saleExpenseDao: SaleExpenseDao,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        saleNumber: String,
        platform: String,
        warehouseId: String,
        items: List<ItemToSell>,
        expenses: List<ExpenseToAdd>,
        saleId: String? = null
    ): Result<Transaction> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        // Validate
        if (saleNumber.isBlank()) {
            return Result.failure(Exception("Sale number is required"))
        }
        if (platform.isBlank()) {
            return Result.failure(Exception("Platform is required"))
        }
        if (warehouseId.isBlank()) {
            return Result.failure(Exception("Warehouse is required"))
        }
        if (items.isEmpty()) {
            return Result.failure(Exception("At least one item is required"))
        }

        // Handle Update vs Create
        return try {
            // Ensure lotId is populated if missing
            val processedItems = items.map { item ->
                if (item.lotId.isBlank()) {
                    val lotIdFromDb = lotRepository.getLotIdForItem(item.itemId)
                    item.copy(lotId = lotIdFromDb ?: "")
                } else {
                    item
                }
            }

            if (saleId != null) {
                // UPDATE FLOW
                val oldItems = saleItemDao.getItemsByTransactionId(saleId)
                
                // Check if we have enough stock for new items, considering old items will be returned to stock
                for (item in processedItems) {
                    val oldItem = oldItems.find { it.itemId == item.itemId }
                    val oldQty = oldItem?.quantity ?: 0
                    val currentStock = stockRepository.getStock(userId, item.itemId, warehouseId)?.quantity ?: 0
                    val effectiveStock = currentStock + oldQty
                    
                    if (effectiveStock < item.quantity) {
                        return Result.failure(Exception("Not enough ${item.itemName} in warehouse (available: $effectiveStock)"))
                    }
                }

                // 1. Revert old stock impact
                oldItems.forEach { oldItem ->
                    stockRepository.incrementQuantity(userId, oldItem.itemId, warehouseId, oldItem.quantity)
                }

                // 2. Apply new stock impact
                processedItems.forEach { newItem ->
                    stockRepository.decrementQuantity(userId, newItem.itemId, warehouseId, newItem.quantity)
                }

                // 3. Update main Transaction record
                val existingTransaction = transactionRepository.getTransactionById(saleId, userId)
                    ?: throw Exception("Sale not found")
                
                val updatedTransaction = existingTransaction.copy(
                    documentNumber = saleNumber,
                    platform = platform,
                    warehouseFromId = warehouseId,
                    amount = processedItems.sumOf { it.sellingPrice * it.quantity } + expenses.sumOf { it.amount },
                    quantity = processedItems.sumOf { it.quantity },
                    updatedAt = System.currentTimeMillis(),
                    notes = "Sale ${processedItems.size} item(s) on $platform"
                )
                transactionRepository.updateTransaction(updatedTransaction)

                // 4. Update items and expenses (Delete old, insert new)
                saleItemDao.deleteItemsByTransactionId(saleId)
                processedItems.forEach { item ->
                    saleItemDao.insertSaleItem(
                        SaleItem(
                            transactionId = saleId,
                            itemId = item.itemId,
                            lotId = item.lotId,
                            quantity = item.quantity,
                            sellingPrice = item.sellingPrice
                        )
                    )
                }

                saleExpenseDao.deleteExpensesByTransactionId(saleId)
                expenses.forEach { expense ->
                    saleExpenseDao.insertSaleExpense(
                        SaleExpense(
                            transactionId = saleId,
                            name = expense.name,
                            amount = expense.amount
                        )
                    )
                }

                Result.success(updatedTransaction)
            } else {
                // CREATE FLOW
                for (item in processedItems) {
                    val stock = stockRepository.getStock(userId, item.itemId, warehouseId)
                    if (stock == null || stock.quantity < item.quantity) {
                        return Result.failure(Exception("Not enough ${item.itemName} in warehouse (available: ${stock?.quantity ?: 0})"))
                    }
                }

                var totalAmount = 0.0
                var totalQuantity = 0

                // Decrement stock for each item
                for (item in processedItems) {
                    stockRepository.decrementQuantity(userId, item.itemId, warehouseId, item.quantity)
                    totalAmount += item.sellingPrice * item.quantity
                    totalQuantity += item.quantity
                }

                val transactionId = java.util.UUID.randomUUID().toString()
                val transaction = Transaction(
                    id = transactionId,
                    userId = userId,
                    type = TransactionType.SALE,
                    status = TransactionStatus.COMPLETED,
                    itemId = processedItems.firstOrNull()?.itemId,
                    lotId = processedItems.firstOrNull()?.lotId,
                    quantity = totalQuantity,
                    amount = totalAmount + expenses.sumOf { it.amount },
                    platform = platform,
                    warehouseFromId = warehouseId,
                    documentNumber = saleNumber,
                    notes = "Sale ${processedItems.size} item(s) on $platform"
                )

                transactionRepository.createTransaction(transaction)

                processedItems.forEach { item ->
                    saleItemDao.insertSaleItem(
                        SaleItem(
                            transactionId = transactionId,
                            itemId = item.itemId,
                            lotId = item.lotId,
                            quantity = item.quantity,
                            sellingPrice = item.sellingPrice
                        )
                    )
                }

                expenses.forEach { expense ->
                    saleExpenseDao.insertSaleExpense(
                        SaleExpense(
                            transactionId = transactionId,
                            name = expense.name,
                            amount = expense.amount
                        )
                    )
                }

                Result.success(transaction)
            }
        } catch (e: Exception) {
            Log.e("CreateSaleUseCase", "Failed to save sale", e)
            Result.failure(e)
        }
    }
}

data class ItemToSell(
    val itemId: String,
    val itemName: String,
    val lotId: String,
    val quantity: Int,
    val sellingPrice: Double
)

data class ExpenseToAdd(
    val name: String,
    val amount: Double
)
