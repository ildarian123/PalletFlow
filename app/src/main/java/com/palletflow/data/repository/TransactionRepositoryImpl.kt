package com.palletflow.data.repository

import com.palletflow.data.local.dao.TransactionDao
import com.palletflow.data.local.entity.Transaction
import com.palletflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    override fun getSalesReport(): Flow<List<Transaction>> = transactionDao.getAllTransactions() // Placeholder
}