package com.palletflow.domain.usecase

import com.palletflow.domain.repository.InventoryRepository
import javax.inject.Inject

class GetStockLevelsUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke() = repository.getStockLevels()
}