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
    override suspend fun insertWarehouse(warehouse: Warehouse) = warehouseDao.insertWarehouse(warehouse)
    override suspend fun deleteWarehouse(warehouse: Warehouse) = warehouseDao.deleteWarehouse(warehouse)
}