package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Warehouse
import kotlinx.coroutines.flow.Flow

interface WarehouseRepository {
    fun getAllWarehouses(userId: String): Flow<List<Warehouse>>
    suspend fun insertWarehouse(warehouse: Warehouse)
    suspend fun deleteWarehouse(warehouse: Warehouse)
}