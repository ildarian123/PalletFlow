package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Warehouse
import kotlinx.coroutines.flow.Flow

interface WarehouseRepository {
    fun getActiveWarehouses(userId: String): Flow<List<Warehouse>>
    fun getAllWarehouses(userId: String): Flow<List<Warehouse>>
    suspend fun getWarehouseById(id: String, userId: String): Warehouse?
    suspend fun createWarehouse(warehouse: Warehouse): Result<Warehouse>
    suspend fun updateWarehouse(warehouse: Warehouse): Result<Warehouse>
    suspend fun deleteWarehouse(id: String, userId: String): Result<Unit>
}
