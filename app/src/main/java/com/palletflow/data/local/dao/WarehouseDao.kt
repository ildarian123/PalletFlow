package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Warehouse
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllWarehousesByUser(userId: String): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveWarehousesByUser(userId: String): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getWarehouseById(id: String, userId: String): Warehouse?

    @Upsert
    suspend fun upsertWarehouse(warehouse: Warehouse)

    @Update
    suspend fun updateWarehouse(warehouse: Warehouse)

    @Query("DELETE FROM warehouses WHERE id = :id AND userId = :userId")
    suspend fun deleteWarehouseById(id: String, userId: String)

    @Query("UPDATE warehouses SET isActive = 0 WHERE id = :id AND userId = :userId")
    suspend fun softDeleteWarehouse(id: String, userId: String)

    @Query("SELECT COUNT(*) FROM warehouses WHERE name = :name AND userId = :userId")
    suspend fun isNameTaken(name: String, userId: String): Int

    @Query("SELECT COUNT(*) FROM warehouses WHERE name = :name AND userId = :userId AND id != :excludeId")
    suspend fun isNameTaken(name: String, userId: String, excludeId: String): Int

    @Delete
    suspend fun deleteWarehouse(warehouse: Warehouse)
}
