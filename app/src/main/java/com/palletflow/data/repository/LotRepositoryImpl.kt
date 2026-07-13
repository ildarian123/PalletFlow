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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    private fun startSync(userId: String) {
        firestoreSyncManager.observeCollection("lots", userId, Lot::class.java)
            .onEach { lots -> lots.forEach { lotDao.insertLot(it) } }
            .launchIn(repositoryScope)
        
        firestoreSyncManager.observeCollection("lot_items", userId, LotItem::class.java)
            .onEach { items -> items.forEach { lotItemDao.insertLotItem(it) } }
            .launchIn(repositoryScope)

        firestoreSyncManager.observeCollection("lot_expenses", userId, LotExpense::class.java)
            .onEach { expenses -> expenses.forEach { lotExpenseDao.insertExpense(it) } }
            .launchIn(repositoryScope)
    }

    override fun getAllLots(userId: String): Flow<List<Lot>> {
        startSync(userId)
        return lotDao.getLotsByUser(userId)
    }

    override suspend fun receiveNewLot(lot: Lot, items: List<LotItem>) {
        lotDao.insertLot(lot)
        items.forEach { lotItemDao.insertLotItem(it) }
        
        firestoreSyncManager.syncToCloud("lots", lot.id, lot)
        items.forEach { firestoreSyncManager.syncToCloud("lot_items", it.id, it) }
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

                lotDao.insertLot(lot)

                val lotItems = items.map { item ->
                    LotItem(
                        lotId = lot.id,
                        itemId = item.itemId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice
                    )
                }
                lotItems.forEach { lotItemDao.insertLotItem(it) }

                val lotExpenses = expenses.map { expense ->
                    LotExpense(
                        lotId = lot.id,
                        name = expense.name,
                        amount = expense.amount
                    )
                }
                lotExpenses.forEach { lotExpenseDao.insertExpense(it) }

                val stocksToSync = mutableListOf<Stock>()
                items.forEach { item ->
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
                        stockDao.insertStock(stock)
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
                transactionDao.insertTransaction(transaction)
                
                FirestoreLotSyncData(lotItems, lotExpenses, transaction, stocksToSync)
            }

            firestoreSyncManager.syncToCloud("lots", lot.id, lot)
            result.lotItems.forEach { firestoreSyncManager.syncToCloud("lot_items", it.id, it) }
            result.lotExpenses.forEach { firestoreSyncManager.syncToCloud("lot_expenses", it.id, it) }
            firestoreSyncManager.syncToCloud("transactions", result.transaction.id, result.transaction)
            result.stocks.forEach { firestoreSyncManager.syncToCloud("stocks", it.id, it) }

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
                oldItems.forEach { oldItem ->
                    stockDao.decrementQuantity(lot.userId, oldItem.itemId, lot.warehouseId, oldItem.quantity)
                }

                lotDao.insertLot(lot)
                lotItemDao.deleteItemsByLot(lot.id)
                
                val lotItems = items.map { item ->
                    LotItem(
                        lotId = lot.id,
                        itemId = item.itemId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice
                    )
                }
                lotItems.forEach { lotItemDao.insertLotItem(it) }
                
                val stocksToSync = mutableListOf<Stock>()
                lotItems.forEach { lotItem ->
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
                        stockDao.insertStock(newStock)
                        stocksToSync.add(newStock)
                    }
                }

                lotExpenseDao.deleteExpensesByLot(lot.id)
                val lotExpenses = expenses.map { expense ->
                    val lotExpense = LotExpense(lotId = lot.id, name = expense.name, amount = expense.amount)
                    lotExpenseDao.insertExpense(lotExpense)
                    lotExpense
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
                    transactionDao.insertTransaction(newTransaction)
                    newTransaction
                }
                
                FirestoreLotSyncData(lotItems, lotExpenses, finalTransaction, stocksToSync)
            }
            
            firestoreSyncManager.syncToCloud("lots", lot.id, lot)
            result.lotItems.forEach { firestoreSyncManager.syncToCloud("lot_items", it.id, it) }
            result.lotExpenses.forEach { firestoreSyncManager.syncToCloud("lot_expenses", it.id, it) }
            firestoreSyncManager.syncToCloud("transactions", result.transaction.id, result.transaction)
            result.stocks.forEach { firestoreSyncManager.syncToCloud("stocks", it.id, it) }

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
                items.forEach { item ->
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
            result.lotItems.forEach { firestoreSyncManager.deleteFromCloud("lot_items", it.id) }
            result.lotExpenses.forEach { firestoreSyncManager.deleteFromCloud("lot_expenses", it.id) }
            firestoreSyncManager.deleteFromCloud("transactions", result.transaction.id)
            result.stocks.forEach { firestoreSyncManager.syncToCloud("stocks", it.id, it) }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to delete lot", e)
            Result.failure(e)
        }
    }

    override suspend fun getLotIdForItem(itemId: String): String? = 
        lotItemDao.getLotIdForItem(itemId)
}

data class FirestoreLotSyncData(
    val lotItems: List<LotItem>,
    val lotExpenses: List<LotExpense>,
    val transaction: Transaction,
    val stocks: List<Stock>
)
