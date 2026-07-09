package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.local.dao.StockDao
import com.palletflow.data.local.entity.Stock
import com.palletflow.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao
) : StockRepository {

    override suspend fun getStock(userId: String, itemId: String, warehouseId: String): Stock? =
        stockDao.getStock(userId, itemId, warehouseId)

    override suspend fun incrementQuantity(
        userId: String,
        itemId: String,
        warehouseId: String,
        quantity: Int
    ): Result<Unit> {
        return try {
            stockDao.incrementQuantity(userId, itemId, warehouseId, quantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StockRepository", "Failed to increment quantity", e)
            Result.failure(e)
        }
    }

    override suspend fun decrementQuantity(
        userId: String,
        itemId: String,
        warehouseId: String,
        quantity: Int
    ): Result<Unit> {
        return try {
            stockDao.decrementQuantity(userId, itemId, warehouseId, quantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StockRepository", "Failed to decrement quantity", e)
            Result.failure(e)
        }
    }

    override suspend fun createStock(stock: Stock): Result<Unit> {
        return try {
            stockDao.insertStock(stock)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StockRepository", "Failed to create stock", e)
            Result.failure(e)
        }
    }

    override fun getStocksByUser(userId: String): Flow<List<Stock>> =
        stockDao.getStocksByUser(userId)
}
