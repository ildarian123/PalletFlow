package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Upsert
    suspend fun upsertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT * FROM items WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllItemsByUser(userId: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getItemById(id: String, userId: String): Item?

    @Query("SELECT * FROM items WHERE id = :id AND userId = :userId LIMIT 1")
    fun getItemByIdSync(id: String, userId: String): Item?

    @Query("""
        SELECT * FROM items
        WHERE userId = :userId
        AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchItems(userId: String, query: String): Flow<List<Item>>

    @Query("SELECT COUNT(*) FROM items WHERE userId = :userId")
    suspend fun getItemsCount(userId: String): Int
}
