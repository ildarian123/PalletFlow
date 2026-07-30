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
    private val authPreferences: AuthPreferences,
    private val firestoreSyncManager: com.palletflow.data.firestore.FirestoreSyncManager
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
            if (saleId != null) {
                // UPDATE FLOW
                val oldItems = saleItemDao.getItemsByTransactionId(saleId)
                
                // Check if we have enough stock for new items, considering old items will be returned to stock
                for (item in items) {
                    val oldItemTotal = oldItems.filter { it.itemId == item.itemId }.sumOf { it.quantity }
                    val currentStock = stockRepository.getStock(userId, item.itemId, warehouseId)?.quantity ?: 0
                    val effectiveStock = currentStock + oldItemTotal
                    
                    if (effectiveStock < item.quantity) {
                        return Result.failure(Exception("Not enough ${item.itemName} in warehouse (available: $effectiveStock)"))
                    }
                }

                // 1. Revert old stock impact
                for (oldItem in oldItems) {
                    stockRepository.incrementQuantity(userId, oldItem.itemId, warehouseId, oldItem.quantity)
                }

                // 2. Apply new stock impact
                for (newItem in items) {
                    stockRepository.decrementQuantity(userId, newItem.itemId, warehouseId, newItem.quantity)
                }

                // 3. Distribute sale quantity across Lots using FIFO
                val distributedSaleItems = distributeAcrossLots(userId, saleId, items)

                // 4. Update main Transaction record
                val existingTransaction = transactionRepository.getTransactionById(saleId, userId)
                    ?: throw Exception("Sale not found")
                
                val updatedTransaction = existingTransaction.copy(
                    documentNumber = saleNumber,
                    platform = platform,
                    warehouseFromId = warehouseId,
                    amount = items.sumOf { it.sellingPrice * it.quantity } + expenses.sumOf { it.amount },
                    quantity = items.sumOf { it.quantity },
                    updatedAt = System.currentTimeMillis(),
                    notes = "Sale ${items.size} item(s) on $platform",
                    lotId = distributedSaleItems.firstOrNull()?.lotId // Track primary lot at top level
                )
                transactionRepository.updateTransaction(updatedTransaction)

                // 5. Update items and expenses (Delete old, insert new)
                val oldSaleItems = saleItemDao.getItemsByTransactionId(saleId)
                val oldSaleExpenses = saleExpenseDao.getExpensesByTransactionId(saleId)
                
                for (item in oldSaleItems) firestoreSyncManager.deleteFromCloud("sale_items", item.id)
                for (expense in oldSaleExpenses) firestoreSyncManager.deleteFromCloud("sale_expenses", expense.id)

                saleItemDao.deleteItemsByTransactionId(saleId)
                for (saleItem in distributedSaleItems) {
                    saleItemDao.upsertSaleItem(saleItem)
                    firestoreSyncManager.syncToCloud("sale_items", saleItem.id, saleItem)
                }

                saleExpenseDao.deleteExpensesByTransactionId(saleId)
                for (expense in expenses) {
                    val saleExpense = SaleExpense(
                        userId = userId,
                        transactionId = saleId,
                        name = expense.name,
                        amount = expense.amount
                    )
                    saleExpenseDao.upsertSaleExpense(saleExpense)
                    firestoreSyncManager.syncToCloud("sale_expenses", saleExpense.id, saleExpense)
                }

                Result.success(updatedTransaction)
            } else {
                // CREATE FLOW
                for (item in items) {
                    val stock = stockRepository.getStock(userId, item.itemId, warehouseId)
                    if (stock == null || stock.quantity < item.quantity) {
                        return Result.failure(Exception("Not enough ${item.itemName} in warehouse (available: ${stock?.quantity ?: 0})"))
                    }
                }

                // Apply stock impact
                for (item in items) {
                    stockRepository.decrementQuantity(userId, item.itemId, warehouseId, item.quantity)
                }

                val transactionId = java.util.UUID.randomUUID().toString()
                
                // Distribute across lots
                val distributedSaleItems = distributeAcrossLots(userId, transactionId, items)

                val transaction = Transaction(
                    id = transactionId,
                    userId = userId,
                    type = TransactionType.SALE,
                    status = TransactionStatus.COMPLETED,
                    itemId = items.firstOrNull()?.itemId,
                    lotId = distributedSaleItems.firstOrNull()?.lotId, // Primary lot
                    quantity = items.sumOf { it.quantity },
                    amount = items.sumOf { it.sellingPrice * it.quantity } + expenses.sumOf { it.amount },
                    platform = platform,
                    warehouseFromId = warehouseId,
                    documentNumber = saleNumber,
                    notes = "Sale ${items.size} item(s) on $platform"
                )

                transactionRepository.createTransaction(transaction)

                for (saleItem in distributedSaleItems) {
                    saleItemDao.upsertSaleItem(saleItem)
                    firestoreSyncManager.syncToCloud("sale_items", saleItem.id, saleItem)
                }

                for (expense in expenses) {
                    val saleExpense = SaleExpense(
                        userId = userId,
                        transactionId = transactionId,
                        name = expense.name,
                        amount = expense.amount
                    )
                    saleExpenseDao.upsertSaleExpense(saleExpense)
                    firestoreSyncManager.syncToCloud("sale_expenses", saleExpense.id, saleExpense)
                }

                Result.success(transaction)
            }
        } catch (e: Exception) {
            Log.e("CreateSaleUseCase", "Failed to save sale", e)
            Result.failure(e)
        }
    }

    private suspend fun distributeAcrossLots(
        userId: String,
        transactionId: String,
        itemsToSell: List<ItemToSell>
    ): List<SaleItem> {
        val result = mutableListOf<SaleItem>()
        
        for (item in itemsToSell) {
            val lotItems = lotRepository.getLotItemsByItemSorted(item.itemId)
            val allPastSales = transactionRepository.getSaleItemsByItem(item.itemId)
                .filter { it.transactionId != transactionId }
            
            var remainingToSell = item.quantity
            
            for (lotItem in lotItems) {
                if (remainingToSell <= 0) break
                
                val totalSoldFromThisLot = allPastSales.filter { it.lotId == lotItem.lotId }.sumOf { it.quantity }
                val availableInThisLot = lotItem.quantity - totalSoldFromThisLot
                
                if (availableInThisLot > 0) {
                    val takeFromThisLot = minOf(remainingToSell, availableInThisLot)
                    result.add(SaleItem(
                        userId = userId,
                        transactionId = transactionId,
                        itemId = item.itemId,
                        lotId = lotItem.lotId,
                        quantity = takeFromThisLot,
                        sellingPrice = item.sellingPrice
                    ))
                    remainingToSell -= takeFromThisLot
                }
            }
            
            // Fallback for remaining quantity (e.g. initial stock or over-sell)
            if (remainingToSell > 0) {
                val lastLotId = lotItems.lastOrNull()?.lotId ?: ""
                result.add(SaleItem(
                    userId = userId,
                    transactionId = transactionId,
                    itemId = item.itemId,
                    lotId = lastLotId,
                    quantity = remainingToSell,
                    sellingPrice = item.sellingPrice
                ))
            }
        }
        
        return result
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
