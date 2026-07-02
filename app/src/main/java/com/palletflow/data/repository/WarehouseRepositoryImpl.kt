package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.local.dao.WarehouseDao
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.repository.WarehouseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WarehouseRepositoryImpl @Inject constructor(
    private val warehouseDao: WarehouseDao
) : WarehouseRepository {

    override fun getActiveWarehouses(userId: String): Flow<List<Warehouse>> =
        warehouseDao.getActiveWarehousesByUser(userId)

    override fun getAllWarehouses(userId: String): Flow<List<Warehouse>> =
        warehouseDao.getAllWarehousesByUser(userId)

    override suspend fun getWarehouseById(id: String, userId: String): Warehouse? =
        warehouseDao.getWarehouseById(id, userId)

    override suspend fun createWarehouse(warehouse: Warehouse): Result<Warehouse> {
        return try {
            // Check if name already taken for this user
            if (warehouseDao.isNameTaken(warehouse.name, warehouse.userId) > 0) {
                return Result.failure(Exception("Warehouse name already exists"))
            }
            warehouseDao.insertWarehouse(warehouse)
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
            Result.success(updated)
        } catch (e: Exception) {
            Log.e("WarehouseRepository", "Failed to update warehouse", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteWarehouse(id: String, userId: String): Result<Unit> {
        return try {
            warehouseDao.deleteWarehouseById(id, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WarehouseRepository", "Failed to delete warehouse", e)
            Result.failure(e)
        }
    }
}
