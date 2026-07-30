package com.palletflow.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.*
import com.palletflow.data.local.database.AppDatabase
import com.palletflow.data.local.entity.*
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.usecase.lot.ExpenseToAdd
import com.palletflow.domain.usecase.lot.ItemToReceive
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LotRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val lotDao: LotDao,
    private val lotItemDao: LotItemDao,
    private val lotExpenseDao: LotExpenseDao,
    private val stockDao: StockDao,
    private val transactionDao: TransactionDao,
    private val firestoreSyncManager: FirestoreSyncManager
) : LotRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null
    private var currentSyncUserId: String? = null

    private fun startSync(userId: String) {
        if (currentSyncUserId == userId) return
        
        syncJob?.cancel()
        currentSyncUserId = userId

        val lotsFlow = firestoreSyncManager.observeCollection("lots", userId, Lot::class.java)
        val lotItemsFlow = firestoreSyncManager.observeCollection("lot_items", userId, LotItem::class.java)
        val lotExpensesFlow = firestoreSyncManager.observeCollection("lot_expenses", userId, LotExpense::class.java)

        syncJob = combine(lotsFlow, lotItemsFlow, lotExpensesFlow) { lots, items, expenses ->
            Triple(lots, items, expenses)
        }
        .onEach { (remoteLots, remoteItems, remoteExpenses) ->
            try {
                database.withTransaction {
                    // Sync Lots
                    val remoteLotIds = remoteLots.map { it.id }.toSet()
                    val localLots = lotDao.getLotsByUserSync(userId)
                    for (local in localLots) {
                        if (local.id !in remoteLotIds) lotDao.deleteLot(local)
                    }
                    for (remote in remoteLots) lotDao.upsertLot(remote)

                    // Sync Items
                    val remoteItemIds = remoteItems.map { it.id }.toSet()
                    val localItems = lotItemDao.getLotItemsByUserSync(userId)
                    for (local in localItems) {
                        if (local.id !in remoteItemIds) lotItemDao.deleteLotItem(local)
                    }
                    for (remote in remoteItems) lotItemDao.upsertLotItem(remote)

                    // Sync Expenses
                    val remoteExpenseIds = remoteExpenses.map { it.id }.toSet()
                    val localExpenses = lotExpenseDao.getLotExpensesByUserSync(userId)
                    for (local in localExpenses) {
                        if (local.id !in remoteExpenseIds) lotExpenseDao.deleteExpense(local)
                    }
                    for (remote in remoteExpenses) lotExpenseDao.upsertExpense(remote)
                }
            } catch (e: Exception) {
                Log.e("LotRepository", "Failed to sync lot data", e)
            }
        }
        .launchIn(repositoryScope)
    }

    override fun getAllLots(userId: String): Flow<List<Lot>> {
        startSync(userId)
        return lotDao.getLotsByUser(userId)
    }

    override suspend fun receiveNewLot(lot: Lot, items: List<LotItem>) {
        lotDao.upsertLot(lot)
        val processedItems = items.map { it.copy(userId = lot.userId) }
        for (item in processedItems) {
            lotItemDao.upsertLotItem(item)
            firestoreSyncManager.syncToCloud("lot_items", item.id, item)
        }
        
        firestoreSyncManager.syncToCloud("lots", lot.id, lot)
    }

    override fun getLotDetail(lotId: String): Flow<Lot?> = lotDao.getLotById(lotId)

    override fun getLotItems(lotId: String): Flow<List<LotItem>> = lotItemDao.getItemsByLot(lotId)

    override suspend fun getLotItemsSync(lotId: String): List<LotItem> = lotItemDao.getItemsByLotSync(lotId)

    override fun getLotExpenses(lotId: String): Flow<List<LotExpense>> = lotExpenseDao.getExpensesByLot(lotId)

    override suspend fun createLotWithItems(
        lot: Lot,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>
    ): Result<Lot> {
        Log.d("LotRepository", "createLotWithItems started for lot: ${lot.lotNumber}")
        return try {
            val result = database.withTransaction {
                if (!isLotNumberUnique(lot.lotNumber)) {
                    throw Exception("Lot number already exists")
                }

                lotDao.upsertLot(lot)

                val lotItems = items.map { item ->
                    LotItem(
                        userId = lot.userId,
                        lotId = lot.id,
                        itemId = item.itemId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice
                    )
                }
                for (li in lotItems) lotItemDao.upsertLotItem(li)

                val lotExpenses = expenses.map { expense ->
                    LotExpense(
                        userId = lot.userId,
                        lotId = lot.id,
                        name = expense.name,
                        amount = expense.amount
                    )
                }
                for (le in lotExpenses) lotExpenseDao.upsertExpense(le)

                val stocksToSync = mutableListOf<Stock>()
                for (item in items) {
                    val stock = Stock(
                        userId = lot.userId,
                        itemId = item.itemId,
                        warehouseId = lot.warehouseId,
                        quantity = item.quantity
                    )
                    val existingStock = stockDao.getStock(lot.userId, item.itemId, lot.warehouseId)
                    if (existingStock != null) {
                        stockDao.incrementQuantity(lot.userId, item.itemId, lot.warehouseId, item.quantity)
                        stocksToSync.add(existingStock.copy(quantity = existingStock.quantity + item.quantity))
                    } else {
                        stockDao.upsertStock(stock)
                        stocksToSync.add(stock)
                    }
                }

                val totalItemsCost = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
                val totalExpenses = expenses.sumOf { it.amount }
                
                val transaction = Transaction(
                    userId = lot.userId,
                    type = TransactionType.INCOMING,
                    status = TransactionStatus.COMPLETED,
                    lotId = lot.id,
                    quantity = items.sumOf { it.quantity },
                    amount = lot.purchasePrice + totalItemsCost + totalExpenses,
                    warehouseToId = lot.warehouseId,
                    notes = "Lot ${lot.lotNumber} received"
                )
                transactionDao.upsertTransaction(transaction)
                
                FirestoreLotSyncData(lotItems, lotExpenses, transaction, stocksToSync)
            }

            firestoreSyncManager.syncToCloud("lots", lot.id, lot)
            for (item in result.lotItems) firestoreSyncManager.syncToCloud("lot_items", item.id, item)
            for (expense in result.lotExpenses) firestoreSyncManager.syncToCloud("lot_expenses", expense.id, expense)
            firestoreSyncManager.syncToCloud("transactions", result.transaction.id, result.transaction)
            for (stock in result.stocks) firestoreSyncManager.syncToCloud("stocks", stock.id, stock)

            Result.success(lot)
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to create lot with items", e)
            Result.failure(e)
        }
    }

    override suspend fun isLotNumberUnique(lotNumber: String): Boolean {
        return lotDao.getLotByNumber(lotNumber) == null
    }

    override suspend fun updateLotWithItems(
        lot: Lot,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>
    ): Result<Lot> {
        return try {
            val result = database.withTransaction {
                val oldItems = lotItemDao.getItemsByLotSync(lot.id)
                val oldExpenses = lotExpenseDao.getExpensesByLotSync(lot.id)
                
                for (oldItem in oldItems) {
                    stockDao.decrementQuantity(lot.userId, oldItem.itemId, lot.warehouseId, oldItem.quantity)
                }

                lotDao.upsertLot(lot)
                lotItemDao.deleteItemsByLot(lot.id)
                
                val lotItems = items.map { item ->
                    LotItem(
                        userId = lot.userId,
                        lotId = lot.id,
                        itemId = item.itemId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice
                    )
                }
                for (li in lotItems) lotItemDao.upsertLotItem(li)
                
                val stocksToSync = mutableListOf<Stock>()
                for (lotItem in lotItems) {
                    val existingStock = stockDao.getStock(lot.userId, lotItem.itemId, lot.warehouseId)
                    if (existingStock != null) {
                        stockDao.incrementQuantity(lot.userId, lotItem.itemId, lot.warehouseId, lotItem.quantity)
                        stocksToSync.add(existingStock.copy(quantity = existingStock.quantity + lotItem.quantity))
                    } else {
                        val newStock = Stock(
                            userId = lot.userId,
                            itemId = lotItem.itemId,
                            warehouseId = lot.warehouseId,
                            quantity = lotItem.quantity
                        )
                        stockDao.upsertStock(newStock)
                        stocksToSync.add(newStock)
                    }
                }

                lotExpenseDao.deleteExpensesByLot(lot.id)
                val lotExpenses = mutableListOf<LotExpense>()
                for (expense in expenses) {
                    val lotExpense = LotExpense(userId = lot.userId, lotId = lot.id, name = expense.name, amount = expense.amount)
                    lotExpenseDao.upsertExpense(lotExpense)
                    lotExpenses.add(lotExpense)
                }
                
                val existingTransaction = transactionDao.getTransactionByLotId(lot.id)
                val totalItemsCost = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
                val totalExpenses = expenses.sumOf { it.amount }
                val totalAmount = lot.purchasePrice + totalItemsCost + totalExpenses

                val finalTransaction = if (existingTransaction != null) {
                    val updatedTransaction = existingTransaction.copy(
                        quantity = items.sumOf { it.quantity },
                        amount = totalAmount,
                        warehouseToId = lot.warehouseId,
                        updatedAt = System.currentTimeMillis()
                    )
                    transactionDao.updateTransaction(updatedTransaction)
                    updatedTransaction
                } else {
                    val newTransaction = Transaction(
                        userId = lot.userId, type = TransactionType.INCOMING,
                        status = TransactionStatus.COMPLETED, lotId = lot.id,
                        quantity = items.sumOf { it.quantity }, amount = totalAmount,
                        warehouseToId = lot.warehouseId, notes = "Lot ${lot.lotNumber} received"
                    )
                    transactionDao.upsertTransaction(newTransaction)
                    newTransaction
                }
                
                FirestoreLotSyncData(
                    lotItems = lotItems,
                    lotExpenses = lotExpenses,
                    transaction = finalTransaction,
                    stocks = stocksToSync,
                    deletedLotItems = oldItems,
                    deletedLotExpenses = oldExpenses
                )
            }
            
            // Cloud Cleanup: Delete old records
            for (item in result.deletedLotItems) firestoreSyncManager.deleteFromCloud("lot_items", item.id)
            for (expense in result.deletedLotExpenses) firestoreSyncManager.deleteFromCloud("lot_expenses", expense.id)

            // Cloud Sync: Upload new/updated records
            firestoreSyncManager.syncToCloud("lots", lot.id, lot)
            for (item in result.lotItems) firestoreSyncManager.syncToCloud("lot_items", item.id, item)
            for (expense in result.lotExpenses) firestoreSyncManager.syncToCloud("lot_expenses", expense.id, expense)
            firestoreSyncManager.syncToCloud("transactions", result.transaction.id, result.transaction)
            for (stock in result.stocks) firestoreSyncManager.syncToCloud("stocks", stock.id, stock)

            Result.success(lot)
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to update lot with items", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteLot(lotId: String, userId: String): Result<Unit> {
        return try {
            val result = database.withTransaction {
                val lot = lotDao.getLotByIdSync(lotId) ?: throw Exception("Lot not found")
                val items = lotItemDao.getItemsByLotSync(lotId)
                val expenses = lotExpenseDao.getExpensesByLotSync(lotId)
                val transaction = transactionDao.getTransactionByLotId(lotId)
                
                val stocksToSync = mutableListOf<Stock>()
                for (item in items) {
                    stockDao.decrementQuantity(userId, item.itemId, lot.warehouseId, item.quantity)
                    val currentStock = stockDao.getStock(userId, item.itemId, lot.warehouseId)
                    if (currentStock != null) stocksToSync.add(currentStock)
                }
                
                lotItemDao.deleteItemsByLot(lotId)
                lotExpenseDao.deleteExpensesByLot(lotId)
                transactionDao.deleteTransactionByLotId(lotId)
                lotDao.deleteLot(lot)
                
                FirestoreLotSyncData(items, expenses, transaction ?: Transaction(userId = userId, type = TransactionType.INCOMING), stocksToSync)
            }
            
            firestoreSyncManager.deleteFromCloud("lots", lotId)
            for (item in result.lotItems) firestoreSyncManager.deleteFromCloud("lot_items", item.id)
            for (expense in result.lotExpenses) firestoreSyncManager.deleteFromCloud("lot_expenses", expense.id)
            firestoreSyncManager.deleteFromCloud("transactions", result.transaction.id)
            for (stock in result.stocks) firestoreSyncManager.syncToCloud("stocks", stock.id, stock)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to delete lot", e)
            Result.failure(e)
        }
    }

    override suspend fun getLotIdForItem(itemId: String): String? = 
        lotItemDao.getLotIdForItem(itemId)

    override suspend fun getLotItemsByItemSorted(itemId: String): List<LotItem> =
        lotItemDao.getLotItemsByItemSorted(itemId)
}

data class FirestoreLotSyncData(
    val lotItems: List<LotItem>,
    val lotExpenses: List<LotExpense>,
    val transaction: Transaction,
    val stocks: List<Stock>,
    val deletedLotItems: List<LotItem> = emptyList(),
    val deletedLotExpenses: List<LotExpense> = emptyList()
)
