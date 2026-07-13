package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Lot
import kotlinx.coroutines.flow.Flow

@Dao
interface LotDao {
    @Query("SELECT * FROM lots")
    fun getAllLots(): Flow<List<Lot>>

    @Query("SELECT * FROM lots WHERE id = :lotId")
    fun getLotById(lotId: String): Flow<Lot?>

    @Query("SELECT * FROM lots WHERE id = :lotId LIMIT 1")
    suspend fun getLotByIdSync(lotId: String): Lot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: Lot)

    @Delete
    suspend fun deleteLot(lot: Lot)

    @Query("SELECT * FROM lots WHERE lotNumber = :lotNumber LIMIT 1")
    suspend fun getLotByNumber(lotNumber: String): Lot?

    @Query("SELECT * FROM lots WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLotsByUser(userId: String): Flow<List<Lot>>
}
