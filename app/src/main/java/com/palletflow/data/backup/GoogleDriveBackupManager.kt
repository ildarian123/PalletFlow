package com.palletflow.data.backup

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import javax.inject.Inject

class GoogleDriveBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authPreferences: AuthPreferences
) {

    private val drive: Drive by lazy {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account?.account
        Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("PalletFlow")
            .build()
    }

    suspend fun backupToDrive(
        backupData: BackupData
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(backupData)
                val encrypted = encryptData(json)

                // Create backup file
                val fileMetadata = File()
                fileMetadata.name = "palletflow_backup_${System.currentTimeMillis()}.json"
                fileMetadata.mimeType = "application/json"
                fileMetadata.parents = listOf("appDataFolder")

                val fileContent = ByteArrayContent("application/json", encrypted.toByteArray())
                
                val createdFile = drive.files()
                    .create(fileMetadata, fileContent)
                    .setFields("id")
                    .execute()

                // Save backup metadata
                authPreferences.saveLastBackupTime(System.currentTimeMillis())

                Result.success(createdFile.id)
            } catch (e: Exception) {
                Log.e("GoogleDriveBackupManager", "Backup failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun restoreFromDrive(): Result<BackupData> {
        return withContext(Dispatchers.IO) {
            try {
                // Get latest backup file from Google Drive
                val fileList = drive.files()
                    .list()
                    .setSpaces("appDataFolder")
                    .setPageSize(1)
                    .setOrderBy("createdTime desc")
                    .setFields("files(id, name)")
                    .execute()

                if (fileList.files.isEmpty()) {
                    return@withContext Result.failure(Exception("No backup found"))
                }

                val backupFileId = fileList.files[0].id

                // Download and decrypt backup
                val content = drive.files()
                    .get(backupFileId)
                    .executeMediaAsInputStream()

                val decrypted = decryptData(content.bufferedReader().readText())
                val backupData = Gson().fromJson(decrypted, BackupData::class.java)

                Result.success(backupData)
            } catch (e: Exception) {
                Log.e("GoogleDriveBackupManager", "Restore failed", e)
                Result.failure(e)
            }
        }
    }

    private fun encryptData(data: String): String {
        // Simple encryption (base64 for MVP, upgrade to AES later)
        return Base64.getEncoder().encodeToString(data.toByteArray())
    }

    private fun decryptData(encrypted: String): String {
        return String(Base64.getDecoder().decode(encrypted))
    }

    fun getLastBackupTime(): Long {
        return authPreferences.getLastBackupTime()
    }
}

data class BackupData(
    val items: List<Item> = emptyList(),
    val warehouses: List<Warehouse> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val lots: List<Lot> = emptyList(),
    val lotItems: List<LotItem> = emptyList(),
    val lotExpenses: List<LotExpense> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val stocks: List<Stock> = emptyList(),
    val saleItems: List<SaleItem> = emptyList(),
    val saleExpenses: List<SaleExpense> = emptyList(),
    val timestamp: Long = 0L
)
