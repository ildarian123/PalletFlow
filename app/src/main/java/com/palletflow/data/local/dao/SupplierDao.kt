package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE userId = :userId")
    fun getAllSuppliersByUser(userId: String): Flow<List<Supplier>>

    @Upsert
    suspend fun upsertSupplier(supplier: Supplier)

    @Query("DELETE FROM suppliers WHERE id = :id AND userId = :userId")
    suspend fun deleteSupplierById(id: String, userId: String)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)
}