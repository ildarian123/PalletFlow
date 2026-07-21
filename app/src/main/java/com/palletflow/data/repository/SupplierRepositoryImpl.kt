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
    private var isSyncStarted = false

    private fun startSync(userId: String) {
        if (isSyncStarted) return
        isSyncStarted = true

        firestoreSyncManager.observeCollection("suppliers", userId, Supplier::class.java)
            .onEach { remoteSuppliers ->
                try {
                    val remoteIds = remoteSuppliers.map { it.id }.toSet()
                    val localSuppliers = supplierDao.getAllSuppliersByUser(userId).first()
                    
                    for (local in localSuppliers) {
                        if (local.id !in remoteIds) {
                            supplierDao.deleteSupplier(local)
                        }
                    }
                    
                    for (remote in remoteSuppliers) {
                        supplierDao.upsertSupplier(remote)
                    }
                } catch (e: Exception) {
                    Log.e("SupplierRepository", "Failed to sync suppliers", e)
                }
            }
            .launchIn(repositoryScope)
    }

    override fun getAllSuppliers(userId: String): Flow<List<Supplier>> {
        startSync(userId)
        return supplierDao.getAllSuppliersByUser(userId)
    }

    override suspend fun createSupplier(supplier: Supplier): Result<Supplier> {
        return try {
            supplierDao.upsertSupplier(supplier)
            firestoreSyncManager.syncToCloud("suppliers", supplier.id, supplier)
            Result.success(supplier)
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Failed to create supplier", e)
            Result.failure(e)
        }
    }

    override suspend fun updateSupplier(supplier: Supplier): Result<Supplier> {
        return try {
            supplierDao.upsertSupplier(supplier)
            firestoreSyncManager.syncToCloud("suppliers", supplier.id, supplier)
            Result.success(supplier)
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Failed to update supplier", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSupplier(id: String, userId: String): Result<Unit> {
        return try {
            supplierDao.deleteSupplierById(id, userId)
            firestoreSyncManager.deleteFromCloud("suppliers", id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Failed to delete supplier", e)
            Result.failure(e)
        }
    }
}
