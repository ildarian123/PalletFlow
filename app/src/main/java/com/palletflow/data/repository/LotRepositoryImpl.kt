package com.palletflow.data.repository

import com.palletflow.data.local.dao.LotDao
import com.palletflow.data.local.dao.LotItemDao
import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotItem
import com.palletflow.domain.repository.LotRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LotRepositoryImpl @Inject constructor(
    private val lotDao: LotDao,
    private val lotItemDao: LotItemDao
) : LotRepository {
    override fun getAllLots(): Flow<List<Lot>> = lotDao.getAllLots()
    override suspend fun receiveNewLot(lot: Lot, items: List<LotItem>) {
        lotDao.insertLot(lot)
        items.forEach { lotItemDao.insertLotItem(it) }
    }
    override fun getLotDetail(lotId: Int): Flow<Lot?> = lotDao.getLotById(lotId)
}