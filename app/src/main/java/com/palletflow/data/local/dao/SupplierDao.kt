package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)
}