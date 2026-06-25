package com.palletflow.domain.usecase

import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotItem
import com.palletflow.domain.repository.LotRepository
import javax.inject.Inject

class ReceiveNewLotUseCase @Inject constructor(
    private val repository: LotRepository
) {
    suspend operator fun invoke(lot: Lot, items: List<LotItem>) = repository.receiveNewLot(lot, items)
}