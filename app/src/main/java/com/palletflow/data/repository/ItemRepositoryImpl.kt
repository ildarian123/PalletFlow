package com.palletflow.data.repository

import com.palletflow.data.local.dao.ItemDao
import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getItems(userId: String): Flow<List<Item>> =
        itemDao.getAllItemsByUser(userId)

    override fun getItem(id: String, userId: String): Flow<Item?> = flow {
        emit(itemDao.getItemById(id, userId))
    }

    override suspend fun createItem(item: Item): Result<Item> {
        return try {
            itemDao.insertItem(item)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItem(item: Item): Result<Item> {
        return try {
            itemDao.updateItem(item)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(item: Item): Result<Unit> {
        return try {
            itemDao.deleteItem(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchItems(userId: String, query: String): Flow<List<Item>> =
        itemDao.searchItems(userId, query)
}
