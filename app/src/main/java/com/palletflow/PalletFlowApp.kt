package com.palletflow

import android.app.Application
import com.palletflow.data.workers.BackupScheduler
import com.palletflow.data.workers.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PalletFlowApp : Application() {
    @Inject
    lateinit var backupScheduler: BackupScheduler

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        backupScheduler.scheduleDaily()
        syncScheduler.schedulePeriodicSync()
    }
}
