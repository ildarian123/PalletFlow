package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(userId: String): Flow<List<Transaction>>
    fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<Transaction>>
    fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String, userId: String): Transaction?
    suspend fun createTransaction(transaction: Transaction): Result<Transaction>
    suspend fun deleteTransaction(id: String, userId: String): Result<Unit>
}
