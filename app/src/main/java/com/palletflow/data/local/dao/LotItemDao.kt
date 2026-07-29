package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.LotItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LotItemDao {
    @Query("SELECT * FROM lot_items")
    fun getAllLotItems(): Flow<List<LotItem>>

    @Upsert
    suspend fun upsertLotItem(lotItem: LotItem)

    @Delete
    suspend fun deleteLotItem(lotItem: LotItem)

    @Query("SELECT * FROM lot_items WHERE lotId = :lotId")
    fun getItemsByLot(lotId: String): Flow<List<LotItem>>

    @Query("SELECT * FROM lot_items WHERE lotId = :lotId")
    suspend fun getItemsByLotSync(lotId: String): List<LotItem>

    @Query("DELETE FROM lot_items WHERE lotId = :lotId")
    suspend fun deleteItemsByLot(lotId: String)

    @Query("""
        SELECT li.* FROM lot_items li
        INNER JOIN lots l ON li.lotId = l.id
        WHERE l.userId = :userId
    """)
    fun getLotItemsByUser(userId: String): Flow<List<LotItem>>

    @Query("""
        SELECT li.* FROM lot_items li
        INNER JOIN lots l ON li.lotId = l.id
        WHERE l.userId = :userId
    """)
    suspend fun getLotItemsByUserSync(userId: String): List<LotItem>

    @Query("SELECT lotId FROM lot_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getLotIdForItem(itemId: String): String?
}