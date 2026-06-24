package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotItem
import kotlinx.coroutines.flow.Flow

interface LotRepository {
    fun getAllLots(): Flow<List<Lot>>
    suspend fun receiveNewLot(lot: Lot, items: List<LotItem>)
    fun getLotDetail(lotId: Int): Flow<Lot?>
}