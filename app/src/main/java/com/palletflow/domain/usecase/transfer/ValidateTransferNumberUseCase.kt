package com.palletflow.domain.usecase.transfer

import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class ValidateTransferNumberUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transferNumber: String): Boolean {
        // Check if transfer number exists in transactions via documentNumber field
        // Return true if unique, false if exists
        return transactionRepository.isDocumentNumberUnique(transferNumber)
    }
}
