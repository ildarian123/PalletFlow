package com.palletflow.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
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
        val userId = authPreferences.getUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Google Drive Backup
                val items = itemDao.getAllItemsByUser(userId).first()
                val lots = lotDao.getLotsByUser(userId).first()
                val transactions = transactionDao.getAllTransactions(userId).first()
                val stocks = stockDao.getStocksByUser(userId).first()

                val result = googleDriveBackupManager.backupToDrive(items, lots, transactions, stocks)
                
                // 2. Firestore Sync (Full Push)
                syncAllToFirestore(userId)

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(isLoading = false, message = "Backup & Cloud Sync successful!")
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, message = "Cloud Sync successful, but Drive backup failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Backup failed: ${e.message}")
                }
            }
        }
    }

    private suspend fun syncAllToFirestore(userId: String) {
        itemDao.getAllItemsByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("items", it.id, it) }
        warehouseDao.getAllWarehousesByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("warehouses", it.id, it) }
        supplierDao.getAllSuppliersByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("suppliers", it.id, it) }
        lotDao.getLotsByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("lots", it.id, it) }
        lotItemDao.getAllLotItems().first().forEach { firestoreSyncManager.syncToCloud("lot_items", it.id, it) }
        lotExpenseDao.getAllExpenses().first().forEach { firestoreSyncManager.syncToCloud("lot_expenses", it.id, it) }
        transactionDao.getAllTransactions(userId).first().forEach { firestoreSyncManager.syncToCloud("transactions", it.id, it) }
        stockDao.getStocksByUser(userId).first().forEach { firestoreSyncManager.syncToCloud("stocks", it.id, it) }
        saleItemDao.getAllSaleItems().forEach { firestoreSyncManager.syncToCloud("sale_items", it.id, it) }
        saleExpenseDao.getAllSaleExpenses().forEach { firestoreSyncManager.syncToCloud("sale_expenses", it.id, it) }
    }

    fun manualRestore() {
        val userId = authPreferences.getUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Try Firestore first as it's the real-time source
                val firestoreResult = firestoreSyncManager.fetchAllUserData(userId)
                if (firestoreResult.isSuccess) {
                    firestoreResult.getOrNull()?.let { data ->
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
                        
                        _uiState.update { it.copy(isLoading = false, message = "Cloud Restore successful!") }
                        return@launch
                    }
                }

                // Fallback to Google Drive
                val result = googleDriveBackupManager.restoreFromDrive()
                if (result.isSuccess) {
                    val backup = result.getOrNull()!!
                    backup.items.forEach { itemDao.insertItem(it) }
                    backup.lots.forEach { lotDao.insertLot(it) }
                    backup.transactions.forEach { transactionDao.insertTransaction(it) }
                    backup.stocks.forEach { stockDao.insertStock(it) }
                    
                    _uiState.update { it.copy(isLoading = false, message = "Restore from Drive successful!") }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Restore failed from both Cloud and Drive") }
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