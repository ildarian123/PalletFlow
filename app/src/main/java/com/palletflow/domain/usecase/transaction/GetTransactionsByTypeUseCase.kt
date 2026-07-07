package com.palletflow.domain.usecase.transaction

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTransactionsByTypeUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke(type: TransactionType): Flow<List<Transaction>> {
        val userId = authPreferences.getUserId()
            ?: return flow { emit(emptyList()) }
        return repository.getTransactionsByType(userId, type)
    }
}
