package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Supplier
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun getAllSuppliers(): Flow<List<Supplier>>
    suspend fun insertSupplier(supplier: Supplier)
}