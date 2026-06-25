package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Lot
import kotlinx.coroutines.flow.Flow

@Dao
interface LotDao {
    @Query("SELECT * FROM lots")
    fun getAllLots(): Flow<List<Lot>>

    @Query("SELECT * FROM lots WHERE id = :lotId")
    fun getLotById(lotId: Int): Flow<Lot?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: Lot)

    @Delete
    suspend fun deleteLot(lot: Lot)
}