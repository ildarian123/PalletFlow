package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.LotExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface LotExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: LotExpense)

    @Update
    suspend fun updateExpense(expense: LotExpense)

    @Delete
    suspend fun deleteExpense(expense: LotExpense)

    @Query("""
        SELECT * FROM lot_expenses 
        WHERE lotId = :lotId
        ORDER BY createdAt DESC
    """)
    fun getExpensesByLot(lotId: String): Flow<List<LotExpense>>

    @Query("""
        SELECT SUM(amount) FROM lot_expenses 
        WHERE lotId = :lotId
    """)
    suspend fun getTotalExpenses(lotId: String): Double

    @Query("""
        DELETE FROM lot_expenses WHERE id = :id
    """)
    suspend fun deleteExpenseById(id: String)
}
