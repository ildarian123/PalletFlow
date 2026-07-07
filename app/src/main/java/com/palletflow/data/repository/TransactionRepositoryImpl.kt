package com.palletflow.data.repository

import android.util.Log
import com.palletflow.data.local.dao.TransactionDao
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(userId: String): Flow<List<Transaction>> =
        transactionDao.getAllTransactions(userId)

    override fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(userId, type)

    override fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(userId, startDate, endDate)

    override suspend fun getTransactionById(id: String, userId: String): Transaction? =
        transactionDao.getTransactionById(id, userId)

    override suspend fun createTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            transactionDao.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to create transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String, userId: String): Result<Unit> {
        return try {
            transactionDao.deleteTransactionById(id, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Failed to delete transaction", e)
            Result.failure(e)
        }
    }
}
