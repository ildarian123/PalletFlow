package com.palletflow.domain.usecase

import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class GetProfitByLotUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke() = repository.getAllTransactions() // Placeholder
}