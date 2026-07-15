package com.palletflow.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.backup.BackupData
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
                val backupData = BackupData(
                    items = itemDao.getAllItemsByUser(userId).first(),
                    warehouses = warehouseDao.getAllWarehousesByUser(userId).first(),
                    suppliers = supplierDao.getAllSuppliersByUser(userId).first(),
                    lots = lotDao.getLotsByUser(userId).first(),
                    lotItems = lotItemDao.getLotItemsByUser(userId).first(),
                    lotExpenses = lotExpenseDao.getLotExpensesByUser(userId).first(),
                    transactions = transactionDao.getAllTransactions(userId).first(),
                    stocks = stockDao.getStocksByUser(userId).first(),
                    saleItems = saleItemDao.getSaleItemsByUser(userId).first(),
                    saleExpenses = saleExpenseDao.getSaleExpensesByUser(userId).first(),
                    timestamp = System.currentTimeMillis()
                )

                val result = googleDriveBackupManager.backupToDrive(backupData)
                
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
        for (item in itemDao.getAllItemsByUser(userId).first()) firestoreSyncManager.syncToCloud("items", item.id, item)
        for (warehouse in warehouseDao.getAllWarehousesByUser(userId).first()) firestoreSyncManager.syncToCloud("warehouses", warehouse.id, warehouse)
        for (supplier in supplierDao.getAllSuppliersByUser(userId).first()) firestoreSyncManager.syncToCloud("suppliers", supplier.id, supplier)
        for (lot in lotDao.getLotsByUser(userId).first()) firestoreSyncManager.syncToCloud("lots", lot.id, lot)
        for (lotItem in lotItemDao.getLotItemsByUser(userId).first()) firestoreSyncManager.syncToCloud("lot_items", lotItem.id, lotItem)
        for (lotExpense in lotExpenseDao.getLotExpensesByUser(userId).first()) firestoreSyncManager.syncToCloud("lot_expenses", lotExpense.id, lotExpense)
        for (transaction in transactionDao.getAllTransactions(userId).first()) firestoreSyncManager.syncToCloud("transactions", transaction.id, transaction)
        for (stock in stockDao.getStocksByUser(userId).first()) firestoreSyncManager.syncToCloud("stocks", stock.id, stock)
        for (saleItem in saleItemDao.getSaleItemsByUser(userId).first()) firestoreSyncManager.syncToCloud("sale_items", saleItem.id, saleItem)
        for (saleExpense in saleExpenseDao.getSaleExpensesByUser(userId).first()) firestoreSyncManager.syncToCloud("sale_expenses", saleExpense.id, saleExpense)
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
                        for (item in data.items) itemDao.upsertItem(item)
                        for (warehouse in data.warehouses) warehouseDao.upsertWarehouse(warehouse)
                        for (supplier in data.suppliers) supplierDao.upsertSupplier(supplier)
                        for (lot in data.lots) lotDao.upsertLot(lot)
                        for (lotItem in data.lotItems) lotItemDao.upsertLotItem(lotItem)
                        for (lotExpense in data.lotExpenses) lotExpenseDao.upsertExpense(lotExpense)
                        for (transaction in data.transactions) transactionDao.upsertTransaction(transaction)
                        for (stock in data.stocks) stockDao.upsertStock(stock)
                        for (saleItem in data.saleItems) saleItemDao.upsertSaleItem(saleItem)
                        for (saleExpense in data.saleExpenses) saleExpenseDao.upsertSaleExpense(saleExpense)
                        
                        _uiState.update { it.copy(isLoading = false, message = "Cloud Restore successful!") }
                        return@launch
                    }
                }

                // Fallback to Google Drive
                val result = googleDriveBackupManager.restoreFromDrive()
                if (result.isSuccess) {
                    val backup = result.getOrNull()!!
                    for (item in backup.items) itemDao.upsertItem(item)
                    for (warehouse in backup.warehouses) warehouseDao.upsertWarehouse(warehouse)
                    for (supplier in backup.suppliers) supplierDao.upsertSupplier(supplier)
                    for (lot in backup.lots) lotDao.upsertLot(lot)
                    for (lotItem in backup.lotItems) lotItemDao.upsertLotItem(lotItem)
                    for (lotExpense in backup.lotExpenses) lotExpenseDao.upsertExpense(lotExpense)
                    for (transaction in backup.transactions) transactionDao.upsertTransaction(transaction)
                    for (stock in backup.stocks) stockDao.upsertStock(stock)
                    for (saleItem in backup.saleItems) saleItemDao.upsertSaleItem(saleItem)
                    for (saleExpense in backup.saleExpenses) saleExpenseDao.upsertSaleExpense(saleExpense)
                    
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