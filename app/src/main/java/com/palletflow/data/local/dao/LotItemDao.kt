package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.LotItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LotItemDao {
    @Query("SELECT * FROM lot_items")
    fun getAllLotItems(): Flow<List<LotItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLotItem(lotItem: LotItem)

    @Delete
    suspend fun deleteLotItem(lotItem: LotItem)
}