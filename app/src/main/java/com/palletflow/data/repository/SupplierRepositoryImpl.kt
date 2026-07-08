package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.local.dao.SupplierDao
import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SupplierRepositoryImpl @Inject constructor(
    private val supplierDao: SupplierDao
) : SupplierRepository {
    override fun getAllSuppliers(userId: String): Flow<List<Supplier>> =
        supplierDao.getAllSuppliersByUser(userId)

    override suspend fun createSupplier(supplier: Supplier): Result<Supplier> {
        return try {
            supplierDao.insertSupplier(supplier)
            Result.success(supplier)
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Failed to create supplier", e)
            Result.failure(e)
        }
    }
}
