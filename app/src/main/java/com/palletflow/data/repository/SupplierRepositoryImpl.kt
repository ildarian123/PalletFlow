package com.palletflow.data.repository

import com.palletflow.data.local.dao.SupplierDao
import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SupplierRepositoryImpl @Inject constructor(
    private val supplierDao: SupplierDao
) : SupplierRepository {
    override fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    override suspend fun insertSupplier(supplier: Supplier) = supplierDao.insertSupplier(supplier)
}