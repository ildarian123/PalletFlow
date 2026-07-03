package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.local.dao.ItemDao
import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import com.palletflow.util.PhotoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    private val photoManager: PhotoManager
) : ItemRepository {

    override fun getAllItems(userId: String): Flow<List<Item>> =
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

    override suspend fun deleteItem(item: Item, deletePhoto: Boolean): Result<Unit> {
        return try {
            itemDao.deleteItem(item)
            if (deletePhoto) {
                photoManager.deletePhoto(item.photoPath)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ItemRepositoryImpl", "Failed to delete item", e)
            Result.failure(e)
        }
    }

    override fun searchItems(userId: String, query: String): Flow<List<Item>> =
        itemDao.searchItems(userId, query)
}
