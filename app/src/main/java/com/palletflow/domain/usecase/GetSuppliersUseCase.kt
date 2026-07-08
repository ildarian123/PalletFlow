package com.palletflow.domain.usecase

import com.palletflow.domain.repository.SupplierRepository
import com.palletflow.data.datastore.AuthPreferences
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetSuppliersUseCase @Inject constructor(
    private val repository: SupplierRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke() = authPreferences.getUserId()?.let { 
        repository.getAllSuppliers(it) 
    } ?: flowOf(emptyList())
}
