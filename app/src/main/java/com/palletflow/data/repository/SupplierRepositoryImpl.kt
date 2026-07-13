package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.SupplierDao
import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.repository.SupplierRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class SupplierRepositoryImpl @Inject constructor(
    private val supplierDao: SupplierDao,
    private val firestoreSyncManager: FirestoreSyncManager
) : SupplierRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun startSync(userId: String) {
        firestoreSyncManager.observeCollection("suppliers", userId, Supplier::class.java)
            .onEach { suppliers ->
                suppliers.forEach { supplierDao.insertSupplier(it) }
            }
            .launchIn(repositoryScope)
    }

    override fun getAllSuppliers(userId: String): Flow<List<Supplier>> {
        startSync(userId)
        return supplierDao.getAllSuppliersByUser(userId)
    }

    override suspend fun createSupplier(supplier: Supplier): Result<Supplier> {
        return try {
            supplierDao.insertSupplier(supplier)
            firestoreSyncManager.syncToCloud("suppliers", supplier.id, supplier)
            Result.success(supplier)
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Failed to create supplier", e)
            Result.failure(e)
        }
    }
}
