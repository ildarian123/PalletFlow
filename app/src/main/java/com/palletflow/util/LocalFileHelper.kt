package com.palletflow.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFileHelper @Inject constructor(
    private val context: Context
) {
    private val TAG = "LocalFileHelper"

    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "item_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image from Uri: $uri", e)
            null
        }
    }

    suspend fun downloadImageToInternalStorage(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "item_remote_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            
            URL(url).openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image from URL: $url", e)
            null
        }
    }
}
