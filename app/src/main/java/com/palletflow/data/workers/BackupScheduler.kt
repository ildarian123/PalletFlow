package com.palletflow.data.workers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BackupScheduler(private val context: Context) {

    fun scheduleDaily() {
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }

    fun cancelSchedule() {
        WorkManager.getInstance(context).cancelUniqueWork("daily_backup")
    }
}
