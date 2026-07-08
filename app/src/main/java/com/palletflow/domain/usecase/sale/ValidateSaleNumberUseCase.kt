package com.palletflow.domain.usecase.sale

import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class ValidateSaleNumberUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(saleNumber: String): Boolean {
        return transactionRepository.isDocumentNumberUnique(saleNumber)
    }
}
