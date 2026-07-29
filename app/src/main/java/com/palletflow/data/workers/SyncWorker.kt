package com.palletflow.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.withTransaction
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.ITEMS
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.LOTS
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.LOT_EXPENSES
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.LOT_ITEMS
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.SALE_EXPENSES
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.SALE_ITEMS
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.STOCKS
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.SUPPLIERS
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.TRANSACTIONS
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.WAREHOUSES
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
        val items = database.itemDao().getAllItemsByUserSync(userId)
        for (item in items) firestoreSyncManager.syncToCloud(ITEMS, item.id, item)
        
        val warehouses = database.warehouseDao().getAllWarehousesByUserSync(userId)
        for (warehouse in warehouses) firestoreSyncManager.syncToCloud(WAREHOUSES, warehouse.id, warehouse)
        
        val suppliers = database.supplierDao().getAllSuppliersByUserSync(userId)
        for (supplier in suppliers) firestoreSyncManager.syncToCloud(SUPPLIERS, supplier.id, supplier)
        
        val lots = database.lotDao().getLotsByUserSync(userId)
        for (lot in lots) firestoreSyncManager.syncToCloud(LOTS, lot.id, lot)
        
        val lotItems = database.lotItemDao().getLotItemsByUserSync(userId)
        for (lotItem in lotItems) firestoreSyncManager.syncToCloud(LOT_ITEMS, lotItem.id, lotItem)
        
        val lotExpenses = database.lotExpenseDao().getLotExpensesByUserSync(userId)
        for (lotExpense in lotExpenses) firestoreSyncManager.syncToCloud(LOT_EXPENSES, lotExpense.id, lotExpense)
        
        val transactions = database.transactionDao().getAllTransactionsSync(userId)
        for (transaction in transactions) firestoreSyncManager.syncToCloud(TRANSACTIONS, transaction.id, transaction)
        
        val stocks = database.stockDao().getStocksByUserSync(userId)
        for (stock in stocks) firestoreSyncManager.syncToCloud(STOCKS, stock.id, stock)
        
        val saleItems = database.saleItemDao().getSaleItemsByUserSync(userId)
        for (saleItem in saleItems) firestoreSyncManager.syncToCloud(SALE_ITEMS, saleItem.id, saleItem)
        
        val saleExpenses = database.saleExpenseDao().getSaleExpensesByUserSync(userId)
        for (saleExpense in saleExpenses) firestoreSyncManager.syncToCloud(SALE_EXPENSES, saleExpense.id, saleExpense)
    }

    private suspend fun syncCloudToLocal(userId: String) {
        val cloudData = firestoreSyncManager.fetchAllUserData(userId).getOrNull() ?: return
        
        database.withTransaction {
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
}
