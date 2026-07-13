package com.palletflow.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val googleDriveBackupManager: GoogleDriveBackupManager,
    private val itemDao: ItemDao,
    private val lotDao: LotDao,
    private val transactionDao: TransactionDao,
    private val stockDao: StockDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun getLastBackupTime(): Long {
        return authPreferences.getLastBackupTime()
    }

    fun isAutoBackupEnabled(): Boolean {
        // Assume enabled for now or add a pref
        return true
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        // Save to prefs
    }

    fun manualBackup() {
        val userId = authPreferences.getGoogleUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val items = itemDao.getAllItemsByUser(userId).first()
                val lots = lotDao.getLotsByUser(userId).first()
                val transactions = transactionDao.getAllTransactions(userId).first()
                val stocks = stockDao.getStocksByUser(userId).first()

                val result = googleDriveBackupManager.backupToDrive(items, lots, transactions, stocks)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(isLoading = false, message = "Backup successful!")
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Backup failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Backup failed: ${e.message}")
                }
            }
        }
    }

    fun manualRestore() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = googleDriveBackupManager.restoreFromDrive()
                if (result.isSuccess) {
                    val backup = result.getOrNull()!!
                    // Restore to DB
                    backup.items.forEach { itemDao.insertItem(it) }
                    backup.lots.forEach { lotDao.insertLot(it) }
                    backup.transactions.forEach { transactionDao.insertTransaction(it) }
                    backup.stocks.forEach { stockDao.insertStock(it) }
                    
                    _uiState.update { it.copy(isLoading = false, message = "Restore successful!") }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Restore failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Restore failed: ${e.message}") }
            }
        }
    }

    fun formatDate(time: Long): String {
        if (time == 0L) return "Never"
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(time))
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)