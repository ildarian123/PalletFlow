package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.LotExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface LotExpenseDao {

    @Upsert
    suspend fun upsertExpense(expense: LotExpense)

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
        DELETE FROM lot_expenses WHERE lotId = :lotId
    """)
    suspend fun deleteExpensesByLot(lotId: String)

    @Query("SELECT * FROM lot_expenses WHERE lotId = :lotId")
    suspend fun getExpensesByLotSync(lotId: String): List<LotExpense>

    @Query("""
        SELECT le.* FROM lot_expenses le
        INNER JOIN lots l ON le.lotId = l.id
        WHERE l.userId = :userId
    """)
    fun getLotExpensesByUser(userId: String): Flow<List<LotExpense>>

    @Query("""
        SELECT le.* FROM lot_expenses le
        INNER JOIN lots l ON le.lotId = l.id
        WHERE l.userId = :userId
    """)
    suspend fun getLotExpensesByUserSync(userId: String): List<LotExpense>

    @Query("SELECT * FROM lot_expenses")
    fun getAllExpenses(): Flow<List<LotExpense>>
}
