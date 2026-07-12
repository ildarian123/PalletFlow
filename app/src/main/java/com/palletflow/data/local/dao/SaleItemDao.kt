package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(saleItem: SaleItem)

    @Query("SELECT * FROM sale_items WHERE transactionId = :transactionId")
    suspend fun getItemsByTransactionId(transactionId: String): List<SaleItem>
    
    @Query("SELECT * FROM sale_items WHERE transactionId = :transactionId")
    fun getItemsByTransactionIdFlow(transactionId: String): Flow<List<SaleItem>>

    @Query("DELETE FROM sale_items WHERE transactionId = :transactionId")
    suspend fun deleteItemsByTransactionId(transactionId: String)

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItems(): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItemsFlow(): Flow<List<SaleItem>>

    @Query("""
        SELECT si.* FROM sale_items si
        INNER JOIN transactions t ON si.transactionId = t.id
        WHERE t.userId = :userId 
        AND t.type = 'SALE'
        AND t.createdAt >= :startDate 
        AND t.createdAt <= :endDate
    """)
    suspend fun getSaleItemsByDateRange(userId: String, startDate: Long, endDate: Long): List<SaleItem>
}
