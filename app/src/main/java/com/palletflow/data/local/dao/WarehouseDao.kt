package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Warehouse
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllWarehousesByUser(userId: String): Flow<List<Warehouse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse)

    @Delete
    suspend fun deleteWarehouse(warehouse: Warehouse)
}