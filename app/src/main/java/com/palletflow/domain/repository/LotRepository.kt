package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotItem
import com.palletflow.domain.usecase.lot.ExpenseToAdd
import com.palletflow.domain.usecase.lot.ItemToReceive
import kotlinx.coroutines.flow.Flow

interface LotRepository {
    fun getAllLots(): Flow<List<Lot>>
    suspend fun receiveNewLot(lot: Lot, items: List<LotItem>)
    fun getLotDetail(lotId: String): Flow<Lot?>

    suspend fun createLotWithItems(
        lot: Lot,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>
    ): Result<Lot>

    suspend fun isLotNumberUnique(lotNumber: String): Boolean
}
