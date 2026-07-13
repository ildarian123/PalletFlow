package com.palletflow.presentation.screens.auth

import android.content.Intent
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.palletflow.data.auth.GoogleAuthManager
import com.palletflow.data.backup.BackupData
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreBackupData
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.*
import com.palletflow.data.util.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao,
    private val googleAuthManager: GoogleAuthManager,
    private val googleDriveBackupManager: GoogleDriveBackupManager,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val itemDao: ItemDao,
    private val warehouseDao: WarehouseDao,
    private val supplierDao: SupplierDao,
    private val lotDao: LotDao,
    private val lotItemDao: LotItemDao,
    private val lotExpenseDao: LotExpenseDao,
    private val transactionDao: TransactionDao,
    private val stockDao: StockDao,
    private val saleItemDao: SaleItemDao,
    private val saleExpenseDao: SaleExpenseDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginEvent = Channel<Boolean>(Channel.BUFFERED)
    val loginEvent = _loginEvent.receiveAsFlow()

    fun getGoogleSignInIntent(): Intent {
        Log.d("LoginViewModel", "getGoogleSignInIntent requested")
        return googleAuthManager.getSignInIntent()
    }

    fun handleGoogleSignIn(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        Log.d("LoginViewModel", "handleGoogleSignIn started")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = googleAuthManager.handleSignInResult(task)
                if (result.isSuccess) {
                    val userProfile = result.getOrNull()!!
                    Log.d("LoginViewModel", "Google Sign-In successful for user: ${userProfile.email}")
                    
                    // Save to local DB if doesn't exist
                    val existingUser = userDao.getUserById(userProfile.id)
                    if (existingUser == null) {
                        Log.d("LoginViewModel", "Creating new local profile for Google user")
                        try {
                            userDao.insertUser(userProfile)
                        } catch (e: Exception) {
                            Log.e("LoginViewModel", "Failed to insert Google user", e)
                        }
                    }

                    authPreferences.saveGoogleUser(userProfile.id, userProfile.email)
                    
                    // Trigger auto-restore from Firestore (Real-time cloud sync)
                    Log.d("LoginViewModel", "Attempting auto-restore from Firestore")
                    val firestoreResult = firestoreSyncManager.fetchAllUserData(userProfile.id)
                    if (firestoreResult.isSuccess) {
                        val data = firestoreResult.getOrNull()
                        if (data != null) {
                            Log.d("LoginViewModel", "Firestore data found, restoring to local DB")
                            restoreFirestoreDataToDatabase(data)
                        }
                    }

                    // Trigger auto-restore from Google Drive
                    Log.d("LoginViewModel", "Attempting auto-restore from Google Drive")
                    val restoreResult = googleDriveBackupManager.restoreFromDrive()
                    if (restoreResult.isSuccess) {
                        Log.d("LoginViewModel", "Restore successful, populating database")
                        val backup = restoreResult.getOrNull()!!
                        restoreBackupToDatabase(backup)
                    } else {
                        Log.d("LoginViewModel", "No backup found or restore failed: ${restoreResult.exceptionOrNull()?.message}")
                    }
                    
                    _uiState.update { it.copy(isLoading = false) }
                    Log.d("LoginViewModel", "Navigating to Dashboard")
                    _loginEvent.trySend(true)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
                    Log.e("LoginViewModel", "Google Sign-In failed: $errorMsg")
                    _uiState.update {
                        it.copy(isLoading = false, error = errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Critical error during Google Sign-In", e)
                _uiState.update {
                    it.copy(isLoading = false, error = "An error occurred: ${e.message}")
                }
            }
        }
    }

    private suspend fun restoreBackupToDatabase(backup: BackupData) {
        // Insert all data from backup
        backup.items.forEach { itemDao.insertItem(it) }
        backup.lots.forEach { lotDao.insertLot(it) }
        backup.transactions.forEach { transactionDao.insertTransaction(it) }
        backup.stocks.forEach { stockDao.insertStock(it) }
    }

    private suspend fun restoreFirestoreDataToDatabase(data: FirestoreBackupData) {
        data.items.forEach { itemDao.insertItem(it) }
        data.warehouses.forEach { warehouseDao.insertWarehouse(it) }
        data.suppliers.forEach { supplierDao.insertSupplier(it) }
        data.lots.forEach { lotDao.insertLot(it) }
        data.lotItems.forEach { lotItemDao.insertLotItem(it) }
        data.lotExpenses.forEach { lotExpenseDao.insertExpense(it) }
        data.transactions.forEach { transactionDao.insertTransaction(it) }
        data.stocks.forEach { stockDao.insertStock(it) }
        data.saleItems.forEach { saleItemDao.insertSaleItem(it) }
        data.saleExpenses.forEach { saleExpenseDao.insertSaleExpense(it) }
    }

    init {
        // Pre-fill email with the last successful login email
        val lastEmail = authPreferences.getLastEmail() ?: ""
        _uiState.update { 
            it.copy(email = lastEmail)
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun login() {
        if (!validate()) return

        val state = _uiState.value
        val hashedPassword = PasswordHasher.hash(state.password)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val authenticatedUser = userDao.getUserByCredentials(state.email, hashedPassword)
            
            if (authenticatedUser != null) {
                saveCredentials(state.email, hashedPassword, authenticatedUser.id)
                _uiState.update { it.copy(isLoading = false) }
                _loginEvent.trySend(true)
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        emailError = "Invalid email or password"
                    )
                }
                _loginEvent.trySend(false)
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }
        
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        }
        
        return isValid
    }

    private fun saveCredentials(email: String, hashedPassword: String, userId: String) {
        authPreferences.saveCredentials(email, hashedPassword, userId)
    }
}

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
