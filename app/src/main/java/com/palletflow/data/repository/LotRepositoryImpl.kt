package com.palletflow.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.palletflow.data.local.dao.*
import com.palletflow.data.local.database.AppDatabase
import com.palletflow.data.local.entity.*
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.usecase.lot.ExpenseToAdd
import com.palletflow.domain.usecase.lot.ItemToReceive
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LotRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val lotDao: LotDao,
    private val lotItemDao: LotItemDao,
    private val lotExpenseDao: LotExpenseDao,
    private val stockDao: StockDao,
    private val transactionDao: TransactionDao
) : LotRepository {
    override fun getAllLots(): Flow<List<Lot>> = lotDao.getAllLots()

    override suspend fun receiveNewLot(lot: Lot, items: List<LotItem>) {
        lotDao.insertLot(lot)
        items.forEach { lotItemDao.insertLotItem(it) }
    }

    override fun getLotDetail(lotId: String): Flow<Lot?> = lotDao.getLotById(lotId)

    override fun getLotItems(lotId: String): Flow<List<LotItem>> = lotItemDao.getItemsByLot(lotId)

    override fun getLotExpenses(lotId: String): Flow<List<LotExpense>> = lotExpenseDao.getExpensesByLot(lotId)

    override suspend fun createLotWithItems(
        lot: Lot,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>
    ): Result<Lot> {
        Log.d("LotRepository", "createLotWithItems started for lot: ${lot.lotNumber}")
        return try {
            database.withTransaction {
                // 1. Validate lot number is unique
                if (!isLotNumberUnique(lot.lotNumber)) {
                    Log.d("LotRepository", "Lot number ${lot.lotNumber} is NOT unique")
                    throw Exception("Lot number already exists")
                }

                // 2. Create Lot
                Log.d("LotRepository", "Inserting lot...")
                lotDao.insertLot(lot)

                // 3. Create LotItem records
                Log.d("LotRepository", "Inserting ${items.size} lot items...")
                items.forEach { item ->
                    val lotItem = LotItem(
                        lotId = lot.id,
                        itemId = item.itemId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice
                    )
                    lotItemDao.insertLotItem(lotItem)
                }

                // 4. Create LotExpense records
                Log.d("LotRepository", "Inserting ${expenses.size} expenses...")
                expenses.forEach { expense ->
                    val lotExpense = LotExpense(
                        lotId = lot.id,
                        name = expense.name,
                        amount = expense.amount
                    )
                    lotExpenseDao.insertExpense(lotExpense)
                }

                // 4. Create Stock records for each item
                Log.d("LotRepository", "Updating stock for ${items.size} items...")
                items.forEach { item ->
                    val stock = Stock(
                        userId = lot.userId,
                        itemId = item.itemId,
                        warehouseId = lot.warehouseId,
                        quantity = item.quantity
                    )
                    // Check if stock exists, if yes increment, if no insert
                    val existingStock = stockDao.getStock(lot.userId, item.itemId, lot.warehouseId)
                    if (existingStock != null) {
                        Log.d("LotRepository", "Incrementing stock for item ${item.itemId}")
                        stockDao.incrementQuantity(
                            lot.userId,
                            item.itemId,
                            lot.warehouseId,
                            item.quantity
                        )
                    } else {
                        Log.d("LotRepository", "Inserting new stock for item ${item.itemId}")
                        stockDao.insertStock(stock)
                    }
                }

                // 5. Create Incoming Transaction
                Log.d("LotRepository", "Creating transaction record...")
                val totalItemsCost = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
                val totalExpenses = expenses.sumOf { it.amount }
                
                val transaction = Transaction(
                    userId = lot.userId,
                    type = TransactionType.INCOMING,
                    status = TransactionStatus.COMPLETED,
                    lotId = lot.id,
                    quantity = items.sumOf { it.quantity },
                    amount = lot.purchasePrice + totalItemsCost + totalExpenses,
                    warehouseFromId = null,
                    warehouseToId = lot.warehouseId,
                    notes = "Lot ${lot.lotNumber} received"
                )
                transactionDao.insertTransaction(transaction)
            }

            Log.d("LotRepository", "createLotWithItems completed successfully")
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
        Log.d("LotRepository", "updateLotWithItems started for lot: ${lot.lotNumber}")
        return try {
            database.withTransaction {
                // 1. Get old items and revert stock impact
                val oldItems = lotItemDao.getItemsByLotSync(lot.id)
                oldItems.forEach { oldItem ->
                    stockDao.decrementQuantity(
                        lot.userId,
                        oldItem.itemId,
                        lot.warehouseId,
                        oldItem.quantity
                    )
                }

                // 2. Update Lot
                lotDao.insertLot(lot) // @Insert(onConflict = REPLACE) handles update

                // 3. Update LotItems (Delete old, Insert new)
                lotItemDao.deleteItemsByLot(lot.id)
                items.forEach { item ->
                    val lotItem = LotItem(
                        lotId = lot.id,
                        itemId = item.itemId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice
                    )
                    lotItemDao.insertLotItem(lotItem)
                    
                    // 4. Apply new stock impact
                    val existingStock = stockDao.getStock(lot.userId, item.itemId, lot.warehouseId)
                    if (existingStock != null) {
                        stockDao.incrementQuantity(
                            lot.userId,
                            item.itemId,
                            lot.warehouseId,
                            item.quantity
                        )
                    } else {
                        val newStock = Stock(
                            userId = lot.userId,
                            itemId = item.itemId,
                            warehouseId = lot.warehouseId,
                            quantity = item.quantity
                        )
                        stockDao.insertStock(newStock)
                    }
                }

                // 5. Update Expenses (Delete old, Insert new)
                lotExpenseDao.deleteExpensesByLot(lot.id)
                expenses.forEach { expense ->
                    val lotExpense = LotExpense(
                        lotId = lot.id,
                        name = expense.name,
                        amount = expense.amount
                    )
                    lotExpenseDao.insertExpense(lotExpense)
                }
                
                // 6. Update Transaction record
                val existingTransaction = transactionDao.getTransactionByLotId(lot.id)
                val totalItemsCost = items.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
                val totalExpenses = expenses.sumOf { it.amount }
                val totalAmount = lot.purchasePrice + totalItemsCost + totalExpenses

                if (existingTransaction != null) {
                    val updatedTransaction = existingTransaction.copy(
                        quantity = items.sumOf { it.quantity },
                        amount = totalAmount,
                        warehouseToId = lot.warehouseId,
                        updatedAt = System.currentTimeMillis()
                    )
                    transactionDao.updateTransaction(updatedTransaction)
                    Log.d("LotRepository", "Updated transaction for lot ${lot.id}")
                } else {
                    // Create if missing for some reason
                    val newTransaction = Transaction(
                        userId = lot.userId,
                        type = TransactionType.INCOMING,
                        status = TransactionStatus.COMPLETED,
                        lotId = lot.id,
                        quantity = items.sumOf { it.quantity },
                        amount = totalAmount,
                        warehouseToId = lot.warehouseId,
                        notes = "Lot ${lot.lotNumber} received"
                    )
                    transactionDao.insertTransaction(newTransaction)
                }
            }
            Log.d("LotRepository", "updateLotWithItems completed successfully")
            Result.success(lot)
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to update lot with items", e)
            Result.failure(e)
        }
    }
}
