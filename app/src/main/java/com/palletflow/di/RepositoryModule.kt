package com.palletflow.di

import com.palletflow.data.repository.*
import com.palletflow.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWarehouseRepository(impl: WarehouseRepositoryImpl): WarehouseRepository

    @Binds
    @Singleton
    abstract fun bindSupplierRepository(impl: SupplierRepositoryImpl): SupplierRepository

    @Binds
    @Singleton
    abstract fun bindLotRepository(impl: LotRepositoryImpl): LotRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository
}