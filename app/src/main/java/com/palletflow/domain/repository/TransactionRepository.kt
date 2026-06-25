package com.palletflow.domain.repository

import com.palletflow.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getSalesReport(): Flow<List<Transaction>>
}