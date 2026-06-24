package com.palletflow.domain.usecase

import com.palletflow.domain.repository.SupplierRepository
import javax.inject.Inject

class GetSuppliersUseCase @Inject constructor(
    private val repository: SupplierRepository
) {
    operator fun invoke() = repository.getAllSuppliers()
}