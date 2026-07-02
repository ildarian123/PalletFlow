package com.palletflow.domain.usecase.warehouse

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.repository.WarehouseRepository
import javax.inject.Inject

class GetWarehouseUseCase @Inject constructor(
    private val repository: WarehouseRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(id: String): Warehouse? {
        val userId = authPreferences.getUserId() ?: return null
        return repository.getWarehouseById(id, userId)
    }
}
