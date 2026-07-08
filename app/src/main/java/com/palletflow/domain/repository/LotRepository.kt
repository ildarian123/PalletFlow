package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotExpense
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

    suspend fun updateLotWithItems(
        lot: Lot,
        items: List<ItemToReceive>,
        expenses: List<ExpenseToAdd>
    ): Result<Lot>

    fun getLotItems(lotId: String): Flow<List<LotItem>>
    fun getLotExpenses(lotId: String): Flow<List<LotExpense>>

    suspend fun isLotNumberUnique(lotNumber: String): Boolean
}
