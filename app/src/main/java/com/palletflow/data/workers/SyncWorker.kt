package com.palletflow.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.database.AppDatabase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val database: AppDatabase,
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
            Log.e("SyncWorker", "Full sync failed", e)
            Result.retry()
        }
    }

    private suspend fun syncLocalToCloud(userId: String) {
        for (item in database.itemDao().getAllItemsByUser(userId).first()) firestoreSyncManager.syncToCloud("items", item.id, item)
        for (warehouse in database.warehouseDao().getAllWarehousesByUser(userId).first()) firestoreSyncManager.syncToCloud("warehouses", warehouse.id, warehouse)
        for (supplier in database.supplierDao().getAllSuppliersByUser(userId).first()) firestoreSyncManager.syncToCloud("suppliers", supplier.id, supplier)
        for (lot in database.lotDao().getLotsByUser(userId).first()) firestoreSyncManager.syncToCloud("lots", lot.id, lot)
        for (lotItem in database.lotItemDao().getLotItemsByUser(userId).first()) firestoreSyncManager.syncToCloud("lot_items", lotItem.id, lotItem)
        for (lotExpense in database.lotExpenseDao().getLotExpensesByUser(userId).first()) firestoreSyncManager.syncToCloud("lot_expenses", lotExpense.id, lotExpense)
        for (transaction in database.transactionDao().getAllTransactions(userId).first()) firestoreSyncManager.syncToCloud("transactions", transaction.id, transaction)
        for (stock in database.stockDao().getStocksByUser(userId).first()) firestoreSyncManager.syncToCloud("stocks", stock.id, stock)
        for (saleItem in database.saleItemDao().getSaleItemsByUser(userId).first()) firestoreSyncManager.syncToCloud("sale_items", saleItem.id, saleItem)
        for (saleExpense in database.saleExpenseDao().getSaleExpensesByUser(userId).first()) firestoreSyncManager.syncToCloud("sale_expenses", saleExpense.id, saleExpense)
    }

    private suspend fun syncCloudToLocal(userId: String) {
        val cloudData = firestoreSyncManager.fetchAllUserData(userId).getOrNull() ?: return
        
        for (item in cloudData.items) database.itemDao().upsertItem(item)
        for (warehouse in cloudData.warehouses) database.warehouseDao().upsertWarehouse(warehouse)
        for (supplier in cloudData.suppliers) database.supplierDao().upsertSupplier(supplier)
        for (lot in cloudData.lots) database.lotDao().upsertLot(lot)
        for (lotItem in cloudData.lotItems) database.lotItemDao().upsertLotItem(lotItem)
        for (lotExpense in cloudData.lotExpenses) database.lotExpenseDao().upsertExpense(lotExpense)
        for (transaction in cloudData.transactions) database.transactionDao().upsertTransaction(transaction)
        for (stock in cloudData.stocks) database.stockDao().upsertStock(stock)
        for (saleItem in cloudData.saleItems) database.saleItemDao().upsertSaleItem(saleItem)
        for (saleExpense in cloudData.saleExpenses) database.saleExpenseDao().upsertSaleExpense(saleExpense)
    }
}
