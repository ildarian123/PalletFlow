package com.palletflow.data.firestore

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageManager @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()
    private val TAG = "FirebaseStorageManager"

    suspend fun uploadItemPhoto(userId: String, itemId: String, localPath: String): String? {
        return try {
            val file = File(localPath)
            if (!file.exists()) {
                Log.e(TAG, "Local file does not exist: $localPath")
                return null
            }

            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("items")
                .child("$itemId.jpg")

            val fileUri = Uri.fromFile(file)
            storageRef.putFile(fileUri).await()
            
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo for item $itemId", e)
            null
        }
    }

    suspend fun deleteItemPhoto(userId: String, itemId: String): Boolean {
        return try {
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("items")
                .child("$itemId.jpg")

            storageRef.delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting photo for item $itemId", e)
            false
        }
    }
}
