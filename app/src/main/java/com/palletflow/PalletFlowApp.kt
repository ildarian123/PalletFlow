package com.palletflow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.palletflow.data.workers.BackupScheduler
import com.palletflow.data.workers.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PalletFlowApp : Application(), Configuration.Provider {
    @Inject
    lateinit var backupScheduler: BackupScheduler

    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        backupScheduler.scheduleDaily()
        syncScheduler.schedulePeriodicSync()
    }
}
