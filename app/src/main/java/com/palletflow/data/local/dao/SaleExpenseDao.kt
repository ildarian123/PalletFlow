package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.SaleExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleExpenseDao {
    @Upsert
    suspend fun upsertSaleExpense(saleExpense: SaleExpense)

    @Delete
    suspend fun deleteSaleExpense(saleExpense: SaleExpense)

    @Query("SELECT * FROM sale_expenses WHERE transactionId = :transactionId")
    suspend fun getExpensesByTransactionId(transactionId: String): List<SaleExpense>
    
    @Query("SELECT * FROM sale_expenses WHERE transactionId = :transactionId")
    fun getExpensesByTransactionIdFlow(transactionId: String): Flow<List<SaleExpense>>

    @Query("DELETE FROM sale_expenses WHERE transactionId = :transactionId")
    suspend fun deleteExpensesByTransactionId(transactionId: String)

    @Query("""
        SELECT se.* FROM sale_expenses se
        INNER JOIN transactions t ON se.transactionId = t.id
        WHERE t.userId = :userId
    """)
    fun getSaleExpensesByUser(userId: String): Flow<List<SaleExpense>>

    @Query("SELECT * FROM sale_expenses")
    suspend fun getAllSaleExpenses(): List<SaleExpense>
}
