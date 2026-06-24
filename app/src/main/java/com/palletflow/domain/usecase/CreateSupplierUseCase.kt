package com.palletflow.domain.usecase

import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.repository.SupplierRepository
import javax.inject.Inject

class CreateSupplierUseCase @Inject constructor(
    private val repository: SupplierRepository
) {
    suspend operator fun invoke(supplier: Supplier) = repository.insertSupplier(supplier)
}