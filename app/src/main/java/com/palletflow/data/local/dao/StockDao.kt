package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.Stock
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: Stock)

    @Update
    suspend fun updateStock(stock: Stock)

    @Query("""
        SELECT * FROM stocks 
        WHERE userId = :userId AND itemId = :itemId AND warehouseId = :warehouseId
        LIMIT 1
    """)
    suspend fun getStock(userId: String, itemId: String, warehouseId: String): Stock?

    @Query("""
        SELECT * FROM stocks 
        WHERE userId = :userId 
        ORDER BY updatedAt DESC
    """)
    fun getStocksByUser(userId: String): Flow<List<Stock>>

    @Query("""
        UPDATE stocks 
        SET quantity = MAX(0, quantity + :quantity), updatedAt = :updatedAt
        WHERE userId = :userId AND itemId = :itemId AND warehouseId = :warehouseId
    """)
    suspend fun incrementQuantity(
        userId: String,
        itemId: String,
        warehouseId: String,
        quantity: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE stocks 
        SET quantity = MAX(0, quantity - :quantity), updatedAt = :updatedAt
        WHERE userId = :userId AND itemId = :itemId AND warehouseId = :warehouseId
    """)
    suspend fun decrementQuantity(
        userId: String,
        itemId: String,
        warehouseId: String,
        quantity: Int,
        updatedAt: Long = System.currentTimeMillis()
    )
}
