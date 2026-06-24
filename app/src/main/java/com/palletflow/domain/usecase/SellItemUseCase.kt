package com.palletflow.domain.usecase

import com.palletflow.domain.repository.InventoryRepository
import javax.inject.Inject

class SellItemUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(itemId: Int, quantity: Int) = repository.sellItem(itemId, quantity)
}