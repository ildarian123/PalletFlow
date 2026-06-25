package com.palletflow.domain.repository

import com.palletflow.data.local.entity.LotItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getStockLevels(): Flow<List<LotItem>>
    suspend fun sellItem(itemId: Int, quantity: Int)
    suspend fun moveInventory(itemId: Int, fromWarehouseId: Int, toWarehouseId: Int)
}