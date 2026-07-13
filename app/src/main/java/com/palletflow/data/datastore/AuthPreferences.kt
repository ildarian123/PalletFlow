package com.palletflow.data.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthPreferences(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = try {
        createSharedPreferences()
    } catch (e: Exception) {
        android.util.Log.e("AuthPreferences", "Error creating EncryptedSharedPreferences", e)
        context.deleteSharedPreferences("auth_prefs")
        createSharedPreferences()
    }

    private fun createSharedPreferences() = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveCredentials(email: String, passwordHash: String, userId: String) {
        sharedPreferences.edit()
            .putString("email", email)
            .putString("password", passwordHash)
            .putString("user_id", userId)
            .putString("last_email", email) // Save as last successful email
            .apply()
    }

    fun getEmail(): String? = sharedPreferences.getString("email", null)

    fun getPassword(): String? = sharedPreferences.getString("password", null)
    
    fun getUserId(): String? = sharedPreferences.getString("user_id", null) ?: sharedPreferences.getString("google_user_id", null)
    
    fun getLastEmail(): String? = sharedPreferences.getString("last_email", null)

    fun saveGoogleUser(userId: String, email: String) {
        sharedPreferences.edit()
            .putString("google_user_id", userId)
            .putString("user_id", userId)
            .putString("email", email)
            .putBoolean("is_google_login", true)
            .apply()
    }

    fun getGoogleUserId(): String? {
        return sharedPreferences.getString("google_user_id", null)
    }

    fun isGoogleLogin(): Boolean {
        return sharedPreferences.getBoolean("is_google_login", false)
    }

    fun saveLastBackupTime(time: Long) {
        sharedPreferences.edit()
            .putLong("last_backup_time", time)
            .apply()
    }

    fun getLastBackupTime(): Long {
        return sharedPreferences.getLong("last_backup_time", 0L)
    }

    fun isLoggedIn(): Boolean {
        if (isGoogleLogin()) {
            return !getGoogleUserId().isNullOrEmpty()
        }
        val email = getEmail()
        val password = getPassword()
        val userId = getUserId()
        return !email.isNullOrEmpty() && !password.isNullOrEmpty() && !userId.isNullOrEmpty()
    }

    fun clearCredentials() {
        sharedPreferences.edit()
            .remove("email")
            .remove("password")
            .remove("user_id")
            .remove("google_user_id")
            .remove("is_google_login")
            .apply()
    }
}
