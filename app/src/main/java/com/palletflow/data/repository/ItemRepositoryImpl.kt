package com.palletflow.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.palletflow.data.firestore.FirebaseStorageManager
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.firestore.FirestoreSyncManager.Companion.ITEMS
import com.palletflow.data.local.dao.ItemDao
import com.palletflow.data.local.database.AppDatabase
import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import com.palletflow.util.LocalFileHelper
import com.palletflow.util.PhotoManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val itemDao: ItemDao,
    private val photoManager: PhotoManager,
    private val localFileHelper: LocalFileHelper,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val storageManager: FirebaseStorageManager
) : ItemRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null
    private var currentSyncUserId: String? = null

    private fun startSync(userId: String) {
        if (currentSyncUserId == userId) return
        
        syncJob?.cancel()
        currentSyncUserId = userId

        syncJob = firestoreSyncManager.observeCollection(ITEMS, userId, Item::class.java)
            .onEach { remoteItems ->
                try {
                    database.withTransaction {
                        val remoteIds = remoteItems.map { it.id }.toSet()
                        val localItems = itemDao.getAllItemsByUserSync(userId)
                        
                        for (local in localItems) {
                            if (local.id !in remoteIds) {
                                itemDao.deleteItem(local)
                            }
                        }

                        for (remoteItem in remoteItems) {
                            val localItem = localItems.find { it.id == remoteItem.id }
                            
                            // If remote has a photo URL and local doesn't have a valid local path
                            val itemToSave = if (remoteItem.photoPath != null && remoteItem.photoPath.startsWith("http")) {
                                if (localItem?.photoPath == null || !File(localItem.photoPath).exists()) {
                                    // Download and save locally
                                    val localPath = localFileHelper.downloadImageToInternalStorage(remoteItem.photoPath)
                                    remoteItem.copy(photoPath = localPath)
                                } else {
                                    // Keep existing local path
                                    remoteItem.copy(photoPath = localItem.photoPath)
                                }
                            } else {
                                remoteItem
                            }
                            
                            itemDao.upsertItem(itemToSave)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ItemRepository", "Failed to sync items", e)
                }
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
            itemDao.upsertItem(item)
            
            // Handle photo upload if exists and is a local path
            val itemToSync = if (item.photoPath != null && !item.photoPath.startsWith("http")) {
                val downloadUrl = storageManager.uploadItemPhoto(item.userId, item.id, item.photoPath)
                if (downloadUrl != null) {
                    val updatedItem = item.copy(photoPath = downloadUrl)
                    itemDao.updateItem(updatedItem) // Update local with URL
                    updatedItem
                } else {
                    item
                }
            } else {
                item
            }

            firestoreSyncManager.syncToCloud(ITEMS, itemToSync.id, itemToSync)
            Result.success(itemToSync)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItem(item: Item): Result<Item> {
        return try {
            itemDao.updateItem(item)

            // Handle photo upload if exists and is a local path
            val itemToSync = if (item.photoPath != null && !item.photoPath.startsWith("http")) {
                val downloadUrl = storageManager.uploadItemPhoto(item.userId, item.id, item.photoPath)
                if (downloadUrl != null) {
                    val updatedItem = item.copy(photoPath = downloadUrl)
                    itemDao.updateItem(updatedItem) // Update local with URL
                    updatedItem
                } else {
                    item
                }
            } else {
                item
            }

            firestoreSyncManager.syncToCloud(ITEMS, itemToSync.id, itemToSync)
            Result.success(itemToSync)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(item: Item, deletePhoto: Boolean): Result<Unit> {
        return try {
            itemDao.deleteItem(item)
            firestoreSyncManager.deleteFromCloud(ITEMS, item.id)
            if (deletePhoto) {
                if (item.photoPath != null) {
                    if (item.photoPath.startsWith("http")) {
                        storageManager.deleteItemPhoto(item.userId, item.id)
                    } else {
                        photoManager.deletePhoto(item.photoPath)
                    }
                }
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
