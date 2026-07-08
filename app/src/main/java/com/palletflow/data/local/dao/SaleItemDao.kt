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
}
