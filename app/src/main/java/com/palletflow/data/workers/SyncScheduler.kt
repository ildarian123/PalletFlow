package com.palletflow.data.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncScheduler(private val context: Context) {

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "cloud_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun cancelSync() {
        WorkManager.getInstance(context).cancelUniqueWork("cloud_sync")
    }
}
