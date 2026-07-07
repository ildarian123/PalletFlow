package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.local.dao.*
import com.palletflow.data.local.entity.*
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.usecase.lot.ExpenseToAdd
import com.palletflow.domain.usecase.lot.ItemToReceive
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LotRepositoryImpl @Inject constructor(
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

    override suspend fun createLotWithItems(
        lot: Lot,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>
    ): Result<Lot> {
        return try {
            // 1. Validate lot number is unique
            if (!isLotNumberUnique(lot.lotNumber)) {
                return Result.failure(Exception("Lot number already exists"))
            }

            // 2. Create Lot
            lotDao.insertLot(lot)

            // 3. Create LotExpense records
            expenses.forEach { expense ->
                val lotExpense = LotExpense(
                    lotId = lot.id,
                    name = expense.name,
                    amount = expense.amount
                )
                lotExpenseDao.insertExpense(lotExpense)
            }

            // 4. Create Stock records for each item
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
                    stockDao.incrementQuantity(
                        lot.userId,
                        item.itemId,
                        lot.warehouseId,
                        item.quantity
                    )
                } else {
                    stockDao.insertStock(stock)
                }
            }

            // 5. Create Incoming Transaction
            val transaction = Transaction(
                userId = lot.userId,
                type = TransactionType.INCOMING,
                status = TransactionStatus.COMPLETED,
                lotId = lot.id,
                quantity = items.sumOf { it.quantity },
                amount = lot.purchasePrice + expenses.sumOf { it.amount },
                warehouseFromId = null,
                warehouseToId = lot.warehouseId,
                notes = "Lot ${lot.lotNumber} received"
            )
            transactionDao.insertTransaction(transaction)

            Result.success(lot)
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to create lot with items", e)
            Result.failure(e)
        }
    }

    override suspend fun isLotNumberUnique(lotNumber: String): Boolean {
        return lotDao.getLotByNumber(lotNumber) == null
    }
}
