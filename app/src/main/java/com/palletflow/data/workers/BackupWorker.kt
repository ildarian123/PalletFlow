package com.palletflow.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.palletflow.data.backup.BackupData
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.database.AppDatabase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BackupWorker(
    context: Context,
    params: WorkerParameters,
    private val backupManager: GoogleDriveBackupManager,
    private val database: AppDatabase,
    private val authPreferences: AuthPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = authPreferences.getGoogleUserId() ?: return Result.failure()

            // Get all data
            val backupData = BackupData(
                items = database.itemDao().getAllItemsByUser(userId).first(),
                warehouses = database.warehouseDao().getAllWarehousesByUser(userId).first(),
                suppliers = database.supplierDao().getAllSuppliersByUser(userId).first(),
                lots = database.lotDao().getLotsByUser(userId).first(),
                lotItems = database.lotItemDao().getLotItemsByUser(userId).first(),
                lotExpenses = database.lotExpenseDao().getLotExpensesByUser(userId).first(),
                transactions = database.transactionDao().getAllTransactions(userId).first(),
                stocks = database.stockDao().getStocksByUser(userId).first(),
                saleItems = database.saleItemDao().getSaleItemsByUser(userId).first(),
                saleExpenses = database.saleExpenseDao().getSaleExpensesByUser(userId).first(),
                timestamp = System.currentTimeMillis()
            )

            // Backup to Google Drive
            val backupResult = backupManager.backupToDrive(backupData)

            if (backupResult.isSuccess) {
                Log.d("BackupWorker", "Backup successful")
                Result.success()
            } else {
                Log.e("BackupWorker", "Backup failed: ${backupResult.exceptionOrNull()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("BackupWorker", "Worker failed", e)
            Result.retry()
        }
    }
}
