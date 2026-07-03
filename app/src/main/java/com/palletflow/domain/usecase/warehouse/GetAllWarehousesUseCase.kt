package com.palletflow.domain.usecase.warehouse

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.repository.WarehouseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllWarehousesUseCase @Inject constructor(
    private val repository: WarehouseRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke(): Flow<List<Warehouse>> {
        val userId = authPreferences.getUserId()
            ?: return flow { emit(emptyList()) }
        return repository.getActiveWarehouses(userId)
    }
}
