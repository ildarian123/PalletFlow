package com.palletflow.domain.usecase.warehouse

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.data.local.entity.WarehouseType
import com.palletflow.domain.repository.WarehouseRepository
import javax.inject.Inject

class CreateWarehouseUseCase @Inject constructor(
    private val repository: WarehouseRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(
        name: String,
        address: String?,
        description: String?,
        type: WarehouseType,
        isActive: Boolean
    ): Result<Warehouse> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        val warehouse = Warehouse(
            userId = userId,
            name = name.trim(),
            address = address?.trim(),
            description = description?.trim(),
            type = type,
            isActive = isActive
        )
        return repository.createWarehouse(warehouse)
    }
}
