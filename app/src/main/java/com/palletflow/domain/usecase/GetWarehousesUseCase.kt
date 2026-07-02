package com.palletflow.domain.usecase

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.repository.WarehouseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetWarehousesUseCase @Inject constructor(
    private val repository: WarehouseRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke(): Flow<List<Warehouse>> {
        val userId = authPreferences.getUserId() ?: return flowOf(emptyList())
        return repository.getAllWarehouses(userId)
    }
}
