package com.palletflow.domain.usecase.transaction

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(transactionId: String): Result<Unit> {
        val userId = authPreferences.getUserId()
            ?: return Result.failure(Exception("User not logged in"))
        return repository.deleteTransaction(transactionId, userId)
    }
}
