package com.palletflow.data.repository

import com.palletflow.data.local.dao.WarehouseDao
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.repository.WarehouseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WarehouseRepositoryImpl @Inject constructor(
    private val warehouseDao: WarehouseDao
) : WarehouseRepository {
    override fun getAllWarehouses(userId: String): Flow<List<Warehouse>> = 
        warehouseDao.getAllWarehousesByUser(userId)

    override fun getActiveWarehouses(userId: String): Flow<List<Warehouse>> =
        warehouseDao.getActiveWarehousesByUser(userId)

    override suspend fun getWarehouseById(id: String, userId: String): Warehouse? =
        warehouseDao.getWarehouseById(id, userId)

    override suspend fun createWarehouse(warehouse: Warehouse): Result<Warehouse> = try {
        warehouseDao.insertWarehouse(warehouse)
        Result.success(warehouse)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateWarehouse(warehouse: Warehouse): Result<Warehouse> = try {
        warehouseDao.updateWarehouse(warehouse)
        Result.success(warehouse)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteWarehouse(id: String, userId: String): Result<Unit> = try {
        warehouseDao.deleteWarehouseById(id, userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
