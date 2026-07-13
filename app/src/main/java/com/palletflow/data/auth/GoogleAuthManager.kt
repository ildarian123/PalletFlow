package com.palletflow.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.palletflow.R
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authPreferences: AuthPreferences
) {

    private val googleSignInClient: GoogleSignInClient by lazy {
        Log.d("GoogleAuthManager", "Initializing GoogleSignInClient")
        val clientId = context.getString(R.string.default_web_client_id)
        Log.d("GoogleAuthManager", "Using client ID: $clientId")
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .requestProfile()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        Log.d("GoogleAuthManager", "getSignInIntent called")
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(
        task: com.google.android.gms.tasks.Task<GoogleSignInAccount>
    ): Result<UserProfile> {
        return try {
            val account = task.getResult(ApiException::class.java)
            
            if (account != null) {
                Log.d("GoogleAuthManager", "Sign-in successful: ${account.email}")
                val userProfile = UserProfile(
                    id = account.id!!,
                    email = account.email!!,
                    businessName = account.displayName ?: "My Business",
                    passwordHash = ""  // Not used for Google Sign-In
                )
                
                Result.success(userProfile)
            } else {
                Log.e("GoogleAuthManager", "Sign-in result: account is null")
                Result.failure(Exception("Account is null"))
            }
        } catch (e: ApiException) {
            val status = com.google.android.gms.common.api.CommonStatusCodes.getStatusCodeString(e.statusCode)
            val detailedError = "Google Error: $status (Code: ${e.statusCode})"
            Log.e("GoogleAuthManager", detailedError, e)
            Result.failure(Exception(detailedError))
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Unexpected error during sign-in result handling", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            authPreferences.clearCredentials()
        }
    }

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
}
