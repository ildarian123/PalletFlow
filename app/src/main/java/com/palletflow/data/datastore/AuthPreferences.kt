package com.palletflow.data.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
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
    
    fun getUserId(): String? = sharedPreferences.getString("user_id", null)
    
    fun getLastEmail(): String? = sharedPreferences.getString("last_email", null)

    fun isLoggedIn(): Boolean {
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
            .apply()
    }
}
