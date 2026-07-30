package com.palletflow.data.workers

import android.util.Log
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorker
import com.palletflow.data.backup.BackupData
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.database.AppDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupManager: GoogleDriveBackupManager,
    private val database: AppDatabase,
    private val authPreferences: AuthPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = authPreferences.getGoogleUserId() ?: return Result.failure()

            // Get all data
            val backupData = BackupData(
                items = database.itemDao().getAllItemsByUserSync(userId),
                warehouses = database.warehouseDao().getAllWarehousesByUserSync(userId),
                suppliers = database.supplierDao().getAllSuppliersByUserSync(userId),
                lots = database.lotDao().getLotsByUserSync(userId),
                lotItems = database.lotItemDao().getLotItemsByUserSync(userId),
                lotExpenses = database.lotExpenseDao().getLotExpensesByUserSync(userId),
                transactions = database.transactionDao().getAllTransactionsSync(userId),
                stocks = database.stockDao().getStocksByUserSync(userId),
                saleItems = database.saleItemDao().getSaleItemsByUserSync(userId),
                saleExpenses = database.saleExpenseDao().getSaleExpensesByUserSync(userId),
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
