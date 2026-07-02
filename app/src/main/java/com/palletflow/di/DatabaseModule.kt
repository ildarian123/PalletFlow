package com.palletflow.di

import android.content.Context
import androidx.room.Room
import com.palletflow.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pallet_flow_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    fun provideWarehouseDao(db: AppDatabase) = db.warehouseDao()

    @Provides
    fun provideSupplierDao(db: AppDatabase) = db.supplierDao()

    @Provides
    fun provideLotDao(db: AppDatabase) = db.lotDao()

    @Provides
    fun provideLotItemDao(db: AppDatabase) = db.lotItemDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase) = db.transactionDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase) = db.expenseDao()
}