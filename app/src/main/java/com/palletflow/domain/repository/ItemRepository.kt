package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(userId: String): Flow<List<Item>>
    fun getItem(id: String, userId: String): Flow<Item?>
    suspend fun createItem(item: Item): Result<Item>
    suspend fun updateItem(item: Item): Result<Item>
    suspend fun deleteItem(item: Item): Result<Unit>
    fun searchItems(userId: String, query: String): Flow<List<Item>>
}
