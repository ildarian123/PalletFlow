package com.palletflow.di

import android.content.Context
import com.palletflow.data.workers.BackupScheduler
import com.palletflow.data.workers.SyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideBackupScheduler(
        @ApplicationContext context: Context
    ): BackupScheduler {
        return BackupScheduler(context)
    }

    @Provides
    @Singleton
    fun provideSyncScheduler(
        @ApplicationContext context: Context
    ): SyncScheduler {
        return SyncScheduler(context)
    }
}
