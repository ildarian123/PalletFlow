package com.palletflow.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.ItemDao
import com.palletflow.data.local.dao.LotDao
import com.palletflow.data.local.dao.StockDao
import com.palletflow.data.local.dao.TransactionDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BackupWorker(
    context: Context,
    params: WorkerParameters,
    private val backupManager: GoogleDriveBackupManager,
    private val itemDao: ItemDao,
    private val lotDao: LotDao,
    private val transactionDao: TransactionDao,
    private val stockDao: StockDao,
    private val authPreferences: AuthPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = authPreferences.getGoogleUserId() ?: return Result.failure()

            // Get all data
            val items = itemDao.getAllItemsByUser(userId).first()
            val lots = lotDao.getLotsByUser(userId).first()
            val transactions = transactionDao.getAllTransactions(userId).first()
            val stocks = stockDao.getStocksByUser(userId).first()

            // Backup to Google Drive
            val backupResult = backupManager.backupToDrive(
                items, lots, transactions, stocks
            )

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
