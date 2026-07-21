package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.StockDao
import com.palletflow.data.local.entity.Stock
import com.palletflow.domain.repository.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao,
    private val firestoreSyncManager: FirestoreSyncManager
) : StockRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun startSync(userId: String) {
        firestoreSyncManager.observeCollection("stocks", userId, Stock::class.java)
            .onEach { remoteStocks ->
                try {
                    val remoteIds = remoteStocks.map { it.id }.toSet()
                    val localStocks = stockDao.getStocksByUser(userId).first()
                    
                    for (local in localStocks) {
                        if (local.id !in remoteIds) {
                            stockDao.deleteStock(local)
                        }
                    }
                    
                    for (remote in remoteStocks) {
                        stockDao.upsertStock(remote)
                    }
                } catch (e: Exception) {
                    Log.e("StockRepository", "Failed to sync stocks", e)
                }
            }
            .launchIn(repositoryScope)
    }

    override suspend fun getStock(userId: String, itemId: String, warehouseId: String): Stock? {
        startSync(userId)
        return stockDao.getStock(userId, itemId, warehouseId)
    }

    override suspend fun incrementQuantity(
        userId: String,
        itemId: String,
        warehouseId: String,
        quantity: Int
    ): Result<Unit> {
        return try {
            stockDao.incrementQuantity(userId, itemId, warehouseId, quantity)
            stockDao.getStock(userId, itemId, warehouseId)?.let {
                firestoreSyncManager.syncToCloud("stocks", it.id, it)
            }
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
            stockDao.getStock(userId, itemId, warehouseId)?.let {
                firestoreSyncManager.syncToCloud("stocks", it.id, it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StockRepository", "Failed to decrement quantity", e)
            Result.failure(e)
        }
    }

    override suspend fun createStock(stock: Stock): Result<Unit> {
        return try {
            val sanitizedStock = if (stock.quantity < 0) stock.copy(quantity = 0) else stock
            stockDao.upsertStock(sanitizedStock)
            firestoreSyncManager.syncToCloud("stocks", sanitizedStock.id, sanitizedStock)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StockRepository", "Failed to create stock", e)
            Result.failure(e)
        }
    }

    override fun getStocksByUser(userId: String): Flow<List<Stock>> {
        startSync(userId)
        return stockDao.getStocksByUser(userId)
    }
}
