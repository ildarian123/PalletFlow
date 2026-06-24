package com.palletflow.domain.usecase

import com.palletflow.domain.repository.LotRepository
import javax.inject.Inject

class GetLotDetailUseCase @Inject constructor(
    private val repository: LotRepository
) {
    operator fun invoke(lotId: Int) = repository.getLotDetail(lotId)
}