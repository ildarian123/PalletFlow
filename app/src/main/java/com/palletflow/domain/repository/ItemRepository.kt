package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getAllItems(userId: String): Flow<List<Item>>
    fun getItem(id: String, userId: String): Flow<Item?>
    suspend fun createItem(item: Item): Result<Item>
    suspend fun updateItem(item: Item): Result<Item>
    suspend fun deleteItem(item: Item, deletePhoto: Boolean = true): Result<Unit>
    fun searchItems(userId: String, query: String): Flow<List<Item>>
}
