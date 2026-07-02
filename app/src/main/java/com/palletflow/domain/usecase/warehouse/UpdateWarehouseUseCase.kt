package com.palletflow.domain.usecase.warehouse

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.data.local.entity.WarehouseType
import com.palletflow.domain.repository.WarehouseRepository
import javax.inject.Inject

class UpdateWarehouseUseCase @Inject constructor(
    private val repository: WarehouseRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        address: String?,
        description: String?,
        type: WarehouseType,
        isActive: Boolean
    ): Result<Warehouse> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        val existingWarehouse = repository.getWarehouseById(id, userId)
            ?: return Result.failure(Exception("Warehouse not found"))

        val updatedWarehouse = existingWarehouse.copy(
            name = name.trim(),
            address = address?.trim(),
            description = description?.trim(),
            type = type,
            isActive = isActive,
            updatedAt = System.currentTimeMillis()
        )
        return repository.updateWarehouse(updatedWarehouse)
    }
}
