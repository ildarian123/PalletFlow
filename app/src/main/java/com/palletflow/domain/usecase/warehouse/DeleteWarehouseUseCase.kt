package com.palletflow.domain.usecase.warehouse

import com.palletflow.domain.repository.WarehouseRepository
import javax.inject.Inject

class DeleteWarehouseUseCase @Inject constructor(
    private val repository: WarehouseRepository
) {
    suspend operator fun invoke(warehouseId: String, userId: String): Result<Unit> {
        return repository.deleteWarehouse(warehouseId, userId)
    }
}
