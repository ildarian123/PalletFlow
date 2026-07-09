package com.palletflow.domain.usecase.transaction

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.data.local.entity.SaleItem
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.StockRepository
import com.palletflow.domain.repository.TransactionRepository
import com.palletflow.data.local.dao.SaleItemDao
import com.palletflow.data.local.dao.SaleExpenseDao
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val lotRepository: LotRepository,
    private val stockRepository: StockRepository,
    private val saleItemDao: SaleItemDao,
    private val saleExpenseDao: SaleExpenseDao,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(transactionId: String): Result<Unit> {
        val userId = authPreferences.getUserId() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val transaction = transactionRepository.getTransactionById(transactionId, userId)
                ?: return Result.failure(Exception("Transaction not found"))

            when (transaction.type) {
                TransactionType.INCOMING -> {
                    if (transaction.lotId != null) {
                        lotRepository.deleteLot(transaction.lotId, userId)
                    } else {
                        transactionRepository.deleteTransaction(transactionId, userId)
                    }
                }
                TransactionType.SALE -> {
                    // 1. Revert stock impact
                    val items = saleItemDao.getItemsByTransactionId(transactionId)
                    val warehouseId = transaction.warehouseFromId
                    if (warehouseId != null) {
                        items.forEach { item ->
                            stockRepository.incrementQuantity(userId, item.itemId, warehouseId, item.quantity)
                        }
                    }

                    // 2. Delete related records
                    saleItemDao.deleteItemsByTransactionId(transactionId)
                    saleExpenseDao.deleteExpensesByTransactionId(transactionId)
                    transactionRepository.deleteTransaction(transactionId, userId)
                }
                TransactionType.TRANSFER -> {
                    // 1. Revert stock impact (return to source, take from dest)
                    val items = saleItemDao.getItemsByTransactionId(transactionId)
                    val sourceId = transaction.warehouseFromId
                    val destId = transaction.warehouseToId

                    if (sourceId != null && destId != null) {
                        items.forEach { item ->
                            stockRepository.incrementQuantity(userId, item.itemId, sourceId, item.quantity)
                            stockRepository.decrementQuantity(userId, item.itemId, destId, item.quantity)
                        }
                    }

                    // 2. Delete related records
                    saleItemDao.deleteItemsByTransactionId(transactionId)
                    transactionRepository.deleteTransaction(transactionId, userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
