package com.palletflow.di

import android.content.Context
import com.palletflow.data.auth.GoogleAuthManager
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.workers.BackupScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideGoogleAuthManager(
        @ApplicationContext context: Context,
        authPreferences: AuthPreferences
    ): GoogleAuthManager {
        return GoogleAuthManager(context, authPreferences)
    }

    @Provides
    @Singleton
    fun provideGoogleDriveBackupManager(
        @ApplicationContext context: Context,
        authPreferences: AuthPreferences
    ): GoogleDriveBackupManager {
        return GoogleDriveBackupManager(context, authPreferences)
    }
}
