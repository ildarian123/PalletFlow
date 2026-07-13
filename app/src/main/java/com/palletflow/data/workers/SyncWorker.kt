package com.palletflow.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val itemDao: ItemDao,
    private val warehouseDao: WarehouseDao,
    private val supplierDao: SupplierDao,
    private val lotDao: LotDao,
    private val lotItemDao: LotItemDao,
    private val lotExpenseDao: LotExpenseDao,
    private val transactionDao: TransactionDao,
    private val stockDao: StockDao,
    private val saleItemDao: SaleItemDao,
    private val saleExpenseDao: SaleExpenseDao,
    private val authPreferences: AuthPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = authPreferences.getUserId() ?: return Result.failure()
        
        return try {
            Log.d("SyncWorker", "Starting full sync for user: $userId")
            
            // Sync local Room to Firestore (Push)
            syncLocalToCloud(userId)
            
            // Sync Firestore to local Room (Pull)
            syncCloudToLocal(userId)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            Result.retry()
        }
    }

    private suspend fun syncLocalToCloud(userId: String) {
        itemDao.getAllItemsByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("items", it.id, it) }
        warehouseDao.getAllWarehousesByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("warehouses", it.id, it) }
        supplierDao.getAllSuppliersByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("suppliers", it.id, it) }
        lotDao.getLotsByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("lots", it.id, it) }
        lotItemDao.getAllLotItems().first().forEach { firestoreSyncManager.syncToCloud("lot_items", it.id, it) }
        lotExpenseDao.getAllExpenses().first().forEach { firestoreSyncManager.syncToCloud("lot_expenses", it.id, it) }
        transactionDao.getAllTransactions(userId).first().forEach { firestoreSyncManager.syncToCloud("transactions", it.id, it) }
        stockDao.getStocksByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("stocks", it.id, it) }
        saleItemDao.getAllSaleItems().forEach { firestoreSyncManager.syncToCloud("sale_items", it.id, it) }
        saleExpenseDao.getAllSaleExpenses().forEach { firestoreSyncManager.syncToCloud("sale_expenses", it.id, it) }
    }

    private suspend fun syncCloudToLocal(userId: String) {
        val cloudData = firestoreSyncManager.fetchAllUserData(userId).getOrNull() ?: return
        
        cloudData.items.forEach { itemDao.insertItem(it) }
        cloudData.warehouses.forEach { warehouseDao.insertWarehouse(it) }
        cloudData.suppliers.forEach { supplierDao.insertSupplier(it) }
        cloudData.lots.forEach { lotDao.insertLot(it) }
        cloudData.lotItems.forEach { lotItemDao.insertLotItem(it) }
        cloudData.lotExpenses.forEach { lotExpenseDao.insertExpense(it) }
        cloudData.transactions.forEach { transactionDao.insertTransaction(it) }
        cloudData.stocks.forEach { stockDao.insertStock(it) }
        cloudData.saleItems.forEach { saleItemDao.insertSaleItem(it) }
        cloudData.saleExpenses.forEach { saleExpenseDao.insertSaleExpense(it) }
    }
}
