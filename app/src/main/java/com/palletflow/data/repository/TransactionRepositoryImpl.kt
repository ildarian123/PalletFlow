package com.palletflow.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.TransactionDao
import com.palletflow.data.local.dao.StockDao
import com.palletflow.data.local.database.AppDatabase
import com.palletflow.data.local.entity.*
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val stockDao: StockDao,
    private val saleItemDao: com.palletflow.data.local.dao.SaleItemDao,
    private val saleExpenseDao: com.palletflow.data.local.dao.SaleExpenseDao,
    private val firestoreSyncManager: FirestoreSyncManager
) : TransactionRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isSyncStarted = false

    private fun startSync(userId: String) {
        if (isSyncStarted) return
        isSyncStarted = true

        firestoreSyncManager.observeCollection("transactions", userId, Transaction::class.java)
            .onEach { remoteTransactions ->
                try {
                    val remoteIds = remoteTransactions.map { it.id }.toSet()
                    val localTransactions = transactionDao.getAllTransactions(userId).first()
                    for (local in localTransactions) {
                        if (local.id !in remoteIds) {
                            transactionDao.deleteTransaction(local)
                        }
                    }
                    for (remote in remoteTransactions) {
                        transactionDao.upsertTransaction(remote)
                    }
                } catch (e: Exception) {
                    Log.e("TransactionRepository", "Failed to sync transactions", e)
                }
            }
            .launchIn(repositoryScope)

        firestoreSyncManager.observeCollection("sale_items", userId, SaleItem::class.java)
            .onEach { remoteItems ->
                try {
                    val remoteIds = remoteItems.map { it.id }.toSet()
                    val localItems = saleItemDao.getSaleItemsByUser(userId).first()
                    for (local in localItems) {
                        if (local.id !in remoteIds) {
                            saleItemDao.deleteSaleItem(local)
                        }
                    }
                    for (remote in remoteItems) {
                        saleItemDao.upsertSaleItem(remote)
                    }
                } catch (e: Exception) {
                    Log.e("TransactionRepository", "Failed to sync sale items", e)
                }
            }
            .launchIn(repositoryScope)

        firestoreSyncManager.observeCollection("sale_expenses", userId, SaleExpense::class.java)
            .onEach { remoteExpenses ->
                try {
                    val remoteIds = remoteExpenses.map { it.id }.toSet()
                    val localExpenses = saleExpenseDao.getSaleExpensesByUser(userId).first()
                    for (local in localExpenses) {
                        if (local.id !in remoteIds) {
                            saleExpenseDao.deleteSaleExpense(local)
                        }
                    }
                    for (remote in remoteExpenses) {
                        saleExpenseDao.upsertSaleExpense(remote)
                    }
                } catch (e: Exception) {
                    Log.e("TransactionRepository", "Failed to sync sale expenses", e)
                }
            }
            .launchIn(repositoryScope)
    }

    override fun getAllTransactions(userId: String): Flow<List<Transaction>> {
        startSync(userId)
        return transactionDao.getAllTransactions(userId)
    }

    override fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<Transaction>> {
        startSync(userId)
        return transactionDao.getTransactionsByType(userId, type)
    }

    override fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> {
        startSync(userId)
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }

    override suspend fun getTransactionById(id: String, userId: String): Transaction? =
        transactionDao.getTransactionById(id, userId)

    override suspend fun isDocumentNumberUnique(documentNumber: String): Boolean =
        transactionDao.getTransactionByDocumentNumber(documentNumber) == null

    override suspend fun createTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            transactionDao.upsertTransaction(transaction)
            firestoreSyncManager.syncToCloud("transactions", transaction.id, transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to create transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            transactionDao.updateTransaction(transaction)
            firestoreSyncManager.syncToCloud("transactions", transaction.id, transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to update transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String, userId: String): Result<Unit> {
        return try {
            // Cleanup related items and expenses if they exist
            val saleItems = saleItemDao.getItemsByTransactionId(id)
            val saleExpenses = saleExpenseDao.getExpensesByTransactionId(id)
            
            for (item in saleItems) firestoreSyncManager.deleteFromCloud("sale_items", item.id)
            for (expense in saleExpenses) firestoreSyncManager.deleteFromCloud("sale_expenses", expense.id)
            
            saleItemDao.deleteItemsByTransactionId(id)
            saleExpenseDao.deleteExpensesByTransactionId(id)
            transactionDao.deleteTransactionById(id, userId)

            firestoreSyncManager.deleteFromCloud("transactions", id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to delete transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSale(id: String, userId: String): Result<Unit> {
        Log.d("TransactionRepository", "deleteSale started for sale: $id")
        return try {
            val result = database.withTransaction {
                val transaction = transactionDao.getTransactionById(id, userId)
                    ?: throw Exception("Sale not found")
                
                val itemsToSync = mutableListOf<SaleItem>()
                val expensesToSync = mutableListOf<SaleExpense>()
                val stocksToSync = mutableListOf<Stock>()

                val warehouseId = transaction.warehouseFromId
                if (warehouseId != null) {
                    val items = saleItemDao.getItemsByTransactionId(id)
                    items.forEach { item ->
                        val existingStock = stockDao.getStock(userId, item.itemId, warehouseId)
                        if (existingStock != null) {
                            stockDao.incrementQuantity(userId, item.itemId, warehouseId, item.quantity)
                            stocksToSync.add(existingStock.copy(quantity = existingStock.quantity + item.quantity))
                        } else {
                            val newStock = Stock(
                                userId = userId,
                                itemId = item.itemId,
                                warehouseId = warehouseId,
                                quantity = item.quantity
                            )
                            stockDao.upsertStock(newStock)
                            stocksToSync.add(newStock)
                        }
                        itemsToSync.add(item)
                    }
                }
                
                val expenses = saleExpenseDao.getExpensesByTransactionId(id)
                expenses.forEach { expensesToSync.add(it) }

                saleItemDao.deleteItemsByTransactionId(id)
                saleExpenseDao.deleteExpensesByTransactionId(id)
                transactionDao.deleteTransactionById(id, userId)
                
                Triple(itemsToSync, expensesToSync, stocksToSync)
            }

            // Cloud deletes
            firestoreSyncManager.deleteFromCloud("transactions", id)
            for (item in result.first) firestoreSyncManager.deleteFromCloud("sale_items", item.id)
            for (expense in result.second) firestoreSyncManager.deleteFromCloud("sale_expenses", expense.id)
            for (stock in result.third) firestoreSyncManager.syncToCloud("stocks", stock.id, stock)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to delete sale", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTransfer(id: String, userId: String): Result<Unit> {
        Log.d("TransactionRepository", "deleteTransfer started for transfer: $id")
        return try {
            val result = database.withTransaction {
                val transaction = transactionDao.getTransactionById(id, userId)
                    ?: throw Exception("Transfer not found")
                
                val itemsToSync = mutableListOf<SaleItem>()
                val stocksToSync = mutableListOf<Stock>()
                
                val sourceId = transaction.warehouseFromId
                val destId = transaction.warehouseToId
                
                if (sourceId != null && destId != null) {
                    val items = saleItemDao.getItemsByTransactionId(id)
                    for (item in items) {
                        // Revert: add back to source
                        val existingSourceStock = stockDao.getStock(userId, item.itemId, sourceId)
                        if (existingSourceStock != null) {
                            stockDao.incrementQuantity(userId, item.itemId, sourceId, item.quantity)
                            stocksToSync.add(existingSourceStock.copy(quantity = existingSourceStock.quantity + item.quantity))
                        } else {
                            val newStock = Stock(
                                userId = userId,
                                itemId = item.itemId,
                                warehouseId = sourceId,
                                quantity = item.quantity
                            )
                            stockDao.upsertStock(newStock)
                            stocksToSync.add(newStock)
                        }
                        
                        // Revert: remove from destination
                        stockDao.decrementQuantity(userId, item.itemId, destId, item.quantity)
                        val currentDestStock = stockDao.getStock(userId, item.itemId, destId)
                        if (currentDestStock != null) stocksToSync.add(currentDestStock)
                        
                        itemsToSync.add(item)
                    }
                }
                
                saleItemDao.deleteItemsByTransactionId(id)
                transactionDao.deleteTransactionById(id, userId)
                
                itemsToSync to stocksToSync
            }

            firestoreSyncManager.deleteFromCloud("transactions", id)
            for (item in result.first) firestoreSyncManager.deleteFromCloud("sale_items", item.id)
            for (stock in result.second) firestoreSyncManager.syncToCloud("stocks", stock.id, stock)

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
