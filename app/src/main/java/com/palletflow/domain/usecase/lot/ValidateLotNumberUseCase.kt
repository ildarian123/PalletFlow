package com.palletflow.domain.usecase.lot

import com.palletflow.domain.repository.LotRepository
import javax.inject.Inject

class ValidateLotNumberUseCase @Inject constructor(
    private val lotRepository: LotRepository
) {
    suspend operator fun invoke(lotNumber: String): Boolean {
        return lotRepository.isLotNumberUnique(lotNumber)
    }
}
