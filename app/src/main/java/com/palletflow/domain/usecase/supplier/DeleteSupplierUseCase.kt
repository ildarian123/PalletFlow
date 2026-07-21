package com.palletflow.domain.usecase.supplier

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.domain.repository.SupplierRepository
import javax.inject.Inject

class DeleteSupplierUseCase @Inject constructor(
    private val repository: SupplierRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(supplierId: String): Result<Unit> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))
            
        return repository.deleteSupplier(supplierId, userId)
    }
}
