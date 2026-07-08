package com.palletflow.domain.usecase.lot

import com.palletflow.data.local.entity.Lot
import com.palletflow.data.local.entity.LotExpense
import com.palletflow.data.local.entity.LotItem
import com.palletflow.domain.repository.LotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetLotFullDetailsUseCase @Inject constructor(
    private val lotRepository: LotRepository
) {
    operator fun invoke(lotId: String): Flow<LotFullDetails?> {
        return combine(
            lotRepository.getLotDetail(lotId),
            lotRepository.getLotItems(lotId),
            lotRepository.getLotExpenses(lotId)
        ) { lot, items, expenses ->
            lot?.let {
                LotFullDetails(it, items, expenses)
            }
        }
    }
}

data class LotFullDetails(
    val lot: Lot,
    val items: List<LotItem>,
    val expenses: List<LotExpense>
)
