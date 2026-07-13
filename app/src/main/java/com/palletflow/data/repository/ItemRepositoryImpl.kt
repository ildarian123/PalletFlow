package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.ItemDao
import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import com.palletflow.util.PhotoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    private val photoManager: PhotoManager,
    private val firestoreSyncManager: FirestoreSyncManager
) : ItemRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isSyncStarted = false

    private fun startSync(userId: String) {
        if (isSyncStarted) return
        isSyncStarted = true

        firestoreSyncManager.observeCollection("items", userId, Item::class.java)
            .onEach { items ->
                items.forEach { itemDao.insertItem(it) }
            }
            .launchIn(repositoryScope)
    }

    override fun getAllItems(userId: String): Flow<List<Item>> {
        startSync(userId)
        return itemDao.getAllItemsByUser(userId)
    }

    override fun getItem(id: String, userId: String): Flow<Item?> = flow {
        emit(itemDao.getItemById(id, userId))
    }

    override suspend fun createItem(item: Item): Result<Item> {
        return try {
            itemDao.insertItem(item)
            firestoreSyncManager.syncToCloud("items", item.id, item)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItem(item: Item): Result<Item> {
        return try {
            itemDao.updateItem(item)
            firestoreSyncManager.syncToCloud("items", item.id, item)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(item: Item, deletePhoto: Boolean): Result<Unit> {
        return try {
            itemDao.deleteItem(item)
            firestoreSyncManager.deleteFromCloud("items", item.id)
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
