package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Warehouse
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses")
    fun getAllWarehouses(): Flow<List<Warehouse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse)

    @Delete
    suspend fun deleteWarehouse(warehouse: Warehouse)
}