package com.palletflow.domain.usecase

import com.palletflow.domain.repository.WarehouseRepository
import javax.inject.Inject

class GetWarehousesUseCase @Inject constructor(
    private val repository: WarehouseRepository
) {
    operator fun invoke() = repository.getAllWarehouses()
}