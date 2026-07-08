package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Stock
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getStock(userId: String, itemId: String, warehouseId: String): Stock?
    suspend fun incrementQuantity(userId: String, itemId: String, warehouseId: String, quantity: Int): Result<Unit>
    suspend fun decrementQuantity(userId: String, itemId: String, warehouseId: String, quantity: Int): Result<Unit>
    fun getStocksByUser(userId: String): Flow<List<Stock>>
}
