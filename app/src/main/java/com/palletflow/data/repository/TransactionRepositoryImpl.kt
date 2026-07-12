package com.palletflow.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.palletflow.data.local.dao.TransactionDao
import com.palletflow.data.local.dao.StockDao
import com.palletflow.data.local.database.AppDatabase
import com.palletflow.data.local.entity.Stock
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.data.local.entity.TransactionStatus
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val stockDao: StockDao,
    private val saleItemDao: com.palletflow.data.local.dao.SaleItemDao,
    private val saleExpenseDao: com.palletflow.data.local.dao.SaleExpenseDao
) : TransactionRepository {

    override fun getAllTransactions(userId: String): Flow<List<Transaction>> =
        transactionDao.getAllTransactions(userId)

    override fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(userId, type)

    override fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(userId, startDate, endDate)

    override suspend fun getTransactionById(id: String, userId: String): Transaction? =
        transactionDao.getTransactionById(id, userId)

    override suspend fun isDocumentNumberUnique(documentNumber: String): Boolean =
        transactionDao.getTransactionByDocumentNumber(documentNumber) == null

    override suspend fun createTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            transactionDao.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to create transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            transactionDao.updateTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to update transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String, userId: String): Result<Unit> {
        return try {
            transactionDao.deleteTransactionById(id, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to delete transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSale(id: String, userId: String): Result<Unit> {
        Log.d("TransactionRepository", "deleteSale started for sale: $id")
        return try {
            database.withTransaction {
                val transaction = transactionDao.getTransactionById(id, userId)
                    ?: throw Exception("Sale not found")
                
                val warehouseId = transaction.warehouseFromId
                if (warehouseId != null) {
                    val items = saleItemDao.getItemsByTransactionId(id)
                    items.forEach { item ->
                        val existingStock = stockDao.getStock(userId, item.itemId, warehouseId)
                        if (existingStock != null) {
                            stockDao.incrementQuantity(userId, item.itemId, warehouseId, item.quantity)
                        } else {
                            stockDao.insertStock(
                                Stock(
                                    userId = userId,
                                    itemId = item.itemId,
                                    warehouseId = warehouseId,
                                    quantity = item.quantity
                                )
                            )
                        }
                    }
                }
                
                saleItemDao.deleteItemsByTransactionId(id)
                saleExpenseDao.deleteExpensesByTransactionId(id)
                transactionDao.deleteTransactionById(id, userId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to delete sale", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTransfer(id: String, userId: String): Result<Unit> {
        Log.d("TransactionRepository", "deleteTransfer started for transfer: $id")
        return try {
            database.withTransaction {
                val transaction = transactionDao.getTransactionById(id, userId)
                    ?: throw Exception("Transfer not found")
                
                val sourceId = transaction.warehouseFromId
                val destId = transaction.warehouseToId
                
                if (sourceId != null && destId != null) {
                    val items = saleItemDao.getItemsByTransactionId(id)
                    items.forEach { item ->
                        // Revert: add back to source
                        val existingSourceStock = stockDao.getStock(userId, item.itemId, sourceId)
                        if (existingSourceStock != null) {
                            stockDao.incrementQuantity(userId, item.itemId, sourceId, item.quantity)
                        } else {
                            stockDao.insertStock(
                                Stock(
                                    userId = userId,
                                    itemId = item.itemId,
                                    warehouseId = sourceId,
                                    quantity = item.quantity
                                )
                            )
                        }
                        
                        // Revert: remove from destination
                        stockDao.decrementQuantity(userId, item.itemId, destId, item.quantity)
                    }
                }
                
                saleItemDao.deleteItemsByTransactionId(id)
                transactionDao.deleteTransactionById(id, userId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to delete transfer", e)
            Result.failure(e)
        }
    }

    override suspend fun getSaleItems(transactionId: String) = 
        saleItemDao.getItemsByTransactionId(transactionId)

    override suspend fun getAllSaleItems() = 
        saleItemDao.getAllSaleItems()

    override fun getAllSaleItemsFlow() = 
        saleItemDao.getAllSaleItemsFlow()

    override suspend fun getSaleItemsByDateRange(userId: String, startDate: Long, endDate: Long) = 
        saleItemDao.getSaleItemsByDateRange(userId, startDate, endDate)

    override suspend fun getSaleExpenses(transactionId: String) = 
        saleExpenseDao.getExpensesByTransactionId(transactionId)
}
