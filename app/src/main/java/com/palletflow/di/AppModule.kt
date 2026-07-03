package com.palletflow.di

import android.content.Context
import com.palletflow.data.datastore.AuthPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAuthPreferences(@ApplicationContext context: Context): AuthPreferences {
        return AuthPreferences(context)
    }

    @Provides
    @Singleton
    fun provideLocalFileHelper(@ApplicationContext context: Context): com.palletflow.util.LocalFileHelper {
        return com.palletflow.util.LocalFileHelper(context)
    }

    @Provides
    @Singleton
    fun providePhotoManager(@ApplicationContext context: Context): com.palletflow.util.PhotoManager {
        return com.palletflow.util.PhotoManager(context)
    }
}