package com.palletflow.domain.usecase

import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.repository.WarehouseRepository
import javax.inject.Inject

class CreateWarehouseUseCase @Inject constructor(
    private val repository: WarehouseRepository
) {
    suspend operator fun invoke(warehouse: Warehouse) = repository.insertWarehouse(warehouse)
}