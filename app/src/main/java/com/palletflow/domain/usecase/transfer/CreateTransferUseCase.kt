package com.palletflow.domain.usecase.transfer

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Stock
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionStatus
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.StockRepository
import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class CreateTransferUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val stockRepository: StockRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        transferNumber: String,
        sourceWarehouseId: String,
        destWarehouseId: String,
        items: List<ItemToTransfer>,
        notes: String? = null
    ): Result<Transaction> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        // Validate
        if (transferNumber.isBlank()) {
            return Result.failure(Exception("Transfer number is required"))
        }
        if (sourceWarehouseId.isBlank()) {
            return Result.failure(Exception("Source warehouse is required"))
        }
        if (destWarehouseId.isBlank()) {
            return Result.failure(Exception("Destination warehouse is required"))
        }
        if (sourceWarehouseId == destWarehouseId) {
            return Result.failure(Exception("Source and destination warehouses must be different"))
        }
        if (items.isEmpty()) {
            return Result.failure(Exception("At least one item is required"))
        }

        // CHECK STOCK AVAILABILITY IN SOURCE WAREHOUSE FOR ALL ITEMS FIRST
        for (item in items) {
            val stock = stockRepository.getStock(userId, item.itemId, sourceWarehouseId)
            if (stock == null || stock.quantity < item.quantity) {
                val itemName = item.itemName
                val sourceName = item.sourceWarehouseName
                return Result.failure(
                    Exception("Not enough $itemName in $sourceName (available: ${stock?.quantity ?: 0})")
                )
            }
        }

        // IF ALL CHECKS PASS, TRANSFER STOCK AND CREATE TRANSACTION
        return try {
            var totalQuantity = 0

            // For each item: decrement source, increment destination
            for (item in items) {
                // Decrement source
                stockRepository.decrementQuantity(
                    userId = userId,
                    itemId = item.itemId,
                    warehouseId = sourceWarehouseId,
                    quantity = item.quantity
                )

                // Increment destination (or create if doesn't exist)
                val destStock = stockRepository.getStock(userId, item.itemId, destWarehouseId)
                if (destStock != null) {
                    stockRepository.incrementQuantity(
                        userId = userId,
                        itemId = item.itemId,
                        warehouseId = destWarehouseId,
                        quantity = item.quantity
                    )
                } else {
                    // Create new Stock record for destination
                    val newStock = Stock(
                        userId = userId,
                        itemId = item.itemId,
                        warehouseId = destWarehouseId,
                        quantity = item.quantity
                    )
                    stockRepository.createStock(newStock)
                }

                totalQuantity += item.quantity
            }

            // Create Transfer Transaction
            val transaction = Transaction(
                userId = userId,
                type = TransactionType.TRANSFER,
                status = TransactionStatus.COMPLETED,
                itemId = items.firstOrNull()?.itemId,  // Primary item
                lotId = items.firstOrNull()?.lotId,    // Primary lot
                quantity = totalQuantity,
                amount = 0.0,  // No amount for transfers
                warehouseFromId = sourceWarehouseId,
                warehouseToId = destWarehouseId,
                documentNumber = transferNumber,
                notes = notes ?: "Transfer ${items.size} item(s) from warehouse to warehouse"
            )

            transactionRepository.createTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("CreateTransferUseCase", "Failed to create transfer", e)
            Result.failure(e)
        }
    }
}

data class ItemToTransfer(
    val itemId: String,
    val itemName: String,
    val lotId: String,
    val quantity: Int,
    val sourceWarehouseId: String,
    val sourceWarehouseName: String
)
