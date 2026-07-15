package com.palletflow

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.*
import com.palletflow.data.local.entity.*
import com.palletflow.presentation.navigation.AppNavGraph
import com.palletflow.ui.theme.PalletFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authPreferences: AuthPreferences

    @Inject
    lateinit var firestoreSyncManager: FirestoreSyncManager

    @Inject
    lateinit var itemDao: ItemDao

    @Inject
    lateinit var warehouseDao: WarehouseDao

    @Inject
    lateinit var supplierDao: SupplierDao

    @Inject
    lateinit var lotDao: LotDao

    @Inject
    lateinit var lotItemDao: LotItemDao

    @Inject
    lateinit var lotExpenseDao: LotExpenseDao

    @Inject
    lateinit var transactionDao: TransactionDao

    @Inject
    lateinit var stockDao: StockDao

    @Inject
    lateinit var saleItemDao: SaleItemDao

    @Inject
    lateinit var saleExpenseDao: SaleExpenseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle Firestore network and Full Restore on Start
        FirebaseFirestore.getInstance().enableNetwork().addOnCompleteListener {
            Log.d("Firestore", "Network enabled")
            restoreAllData()
        }

        setContent {
            PalletFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }

    private fun restoreAllData() {
        val userId = authPreferences.getUserId() ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("MainActivity", "Starting full Firestore restore for user: $userId")
                val result = firestoreSyncManager.fetchAllUserData(userId)
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null) {
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
                        Log.d("MainActivity", "Full Firestore restore completed successfully")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during full Firestore restore", e)
            }
        }
    }
}
