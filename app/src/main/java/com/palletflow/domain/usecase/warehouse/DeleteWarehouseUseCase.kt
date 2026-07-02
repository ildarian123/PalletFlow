package com.palletflow.domain.usecase.warehouse

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.domain.repository.WarehouseRepository
import javax.inject.Inject

class DeleteWarehouseUseCase @Inject constructor(
    private val repository: WarehouseRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))
        return repository.deleteWarehouse(id, userId)
    }
}
