package com.palletflow.data.repository

import com.palletflow.data.local.dao.LotItemDao
import com.palletflow.data.local.entity.LotItem
import com.palletflow.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val lotItemDao: LotItemDao
) : InventoryRepository {
    override fun getStockLevels(): Flow<List<LotItem>> = lotItemDao.getAllLotItems()
    override suspend fun sellItem(itemId: Int, quantity: Int) {}
    override suspend fun moveInventory(itemId: Int, fromWarehouseId: Int, toWarehouseId: Int) {}
}