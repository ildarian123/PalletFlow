package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.WarehouseDao
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.repository.WarehouseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WarehouseRepositoryImpl @Inject constructor(
    private val warehouseDao: WarehouseDao,
    private val firestoreSyncManager: FirestoreSyncManager
) : WarehouseRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isSyncStarted = false

    private fun startSync(userId: String) {
        if (isSyncStarted) return
        isSyncStarted = true

        firestoreSyncManager.observeCollection("warehouses", userId, Warehouse::class.java)
            .onEach { remoteWarehouses ->
                try {
                    val remoteIds = remoteWarehouses.map { it.id }.toSet()
                    val localWarehouses = warehouseDao.getAllWarehousesByUser(userId).first()
                    
                    for (local in localWarehouses) {
                        if (local.id !in remoteIds) {
                            warehouseDao.deleteWarehouse(local)
                        }
                    }
                    
                    for (remote in remoteWarehouses) {
                        warehouseDao.upsertWarehouse(remote)
                    }
                } catch (e: Exception) {
                    Log.e("WarehouseRepository", "Failed to sync warehouses", e)
                }
            }
            .launchIn(repositoryScope)
    }

    override fun getActiveWarehouses(userId: String): Flow<List<Warehouse>> {
        startSync(userId)
        return warehouseDao.getActiveWarehousesByUser(userId)
    }

    override fun getAllWarehouses(userId: String): Flow<List<Warehouse>> {
        startSync(userId)
        return warehouseDao.getAllWarehousesByUser(userId)
    }

    override suspend fun getWarehouseById(id: String, userId: String): Warehouse? =
        warehouseDao.getWarehouseById(id, userId)

    override suspend fun createWarehouse(warehouse: Warehouse): Result<Warehouse> {
        return try {
            // Check if name already taken for this user
            if (warehouseDao.isNameTaken(warehouse.name, warehouse.userId) > 0) {
                return Result.failure(Exception("Warehouse name already exists"))
            }
            warehouseDao.upsertWarehouse(warehouse)
            firestoreSyncManager.syncToCloud("warehouses", warehouse.id, warehouse)
            Result.success(warehouse)
        } catch (e: Exception) {
            Log.e("WarehouseRepository", "Failed to create warehouse", e)
            Result.failure(e)
        }
    }

    override suspend fun updateWarehouse(warehouse: Warehouse): Result<Warehouse> {
        return try {
            // Check if name taken by another warehouse
            if (warehouseDao.isNameTaken(warehouse.name, warehouse.userId, warehouse.id) > 0) {
                return Result.failure(Exception("Warehouse name already exists"))
            }
            val updated = warehouse.copy(updatedAt = System.currentTimeMillis())
            warehouseDao.updateWarehouse(updated)
            firestoreSyncManager.syncToCloud("warehouses", updated.id, updated)
            Result.success(updated)
        } catch (e: Exception) {
            Log.e("WarehouseRepository", "Failed to update warehouse", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteWarehouse(id: String, userId: String): Result<Unit> {
        return try {
            warehouseDao.deleteWarehouseById(id, userId)
            firestoreSyncManager.deleteFromCloud("warehouses", id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WarehouseRepository", "Failed to delete warehouse", e)
            Result.failure(e)
        }
    }
}
