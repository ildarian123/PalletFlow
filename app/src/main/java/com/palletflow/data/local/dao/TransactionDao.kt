package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        ORDER BY createdAt DESC
    """)
    fun getAllTransactions(userId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND type = :type
        ORDER BY createdAt DESC
    """)
    fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND createdAt >= :startDate 
        AND createdAt <= :endDate
        ORDER BY createdAt DESC
    """)
    fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE id = :id AND userId = :userId LIMIT 1
    """)
    suspend fun getTransactionById(id: String, userId: String): Transaction?

    @Query("SELECT * FROM transactions WHERE lotId = :lotId LIMIT 1")
    suspend fun getTransactionByLotId(lotId: String): Transaction?

    @Query("SELECT * FROM transactions WHERE documentNumber = :documentNumber LIMIT 1")
    suspend fun getTransactionByDocumentNumber(documentNumber: String): Transaction?

    @Query("DELETE FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun deleteTransactionById(id: String, userId: String)
}
