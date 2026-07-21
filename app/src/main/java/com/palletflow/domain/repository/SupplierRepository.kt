package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Supplier
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun getAllSuppliers(userId: String): Flow<List<Supplier>>
    suspend fun createSupplier(supplier: Supplier): Result<Supplier>
    suspend fun updateSupplier(supplier: Supplier): Result<Supplier>
    suspend fun deleteSupplier(id: String, userId: String): Result<Unit>
}