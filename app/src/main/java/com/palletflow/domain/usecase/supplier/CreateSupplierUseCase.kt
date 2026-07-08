package com.palletflow.domain.usecase.supplier

import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.repository.SupplierRepository
import com.palletflow.data.datastore.AuthPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateSupplierUseCase @Inject constructor(
    private val repository: SupplierRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(name: String): Result<Supplier> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))

        if (name.isBlank()) {
            return Result.failure(Exception("Supplier name cannot be empty"))
        }

        // Check for duplicates
        val existingSuppliers = repository.getAllSuppliers(userId).first()
        if (existingSuppliers.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("Supplier with this name already exists"))
        }

        val supplier = Supplier(
            userId = userId,
            name = name.trim(),
            contactPerson = null,
            phone = null,
            email = null,
            address = null,
            notes = null,
            isFrequent = false
        )

        return repository.createSupplier(supplier)
    }
}
