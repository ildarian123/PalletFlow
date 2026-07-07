package com.palletflow.domain.usecase

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetSalesReportUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val authPreferences: AuthPreferences
) {
    operator fun invoke() = authPreferences.getUserId()?.let {
        repository.getAllTransactions(it)
    } ?: flow { emit(emptyList()) }
}
