package com.palletflow.domain.usecase

import com.palletflow.domain.repository.InventoryRepository
import javax.inject.Inject

class MoveInventoryUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(itemId: Int, fromWarehouseId: Int, toWarehouseId: Int) = 
        repository.moveInventory(itemId, fromWarehouseId, toWarehouseId)
}