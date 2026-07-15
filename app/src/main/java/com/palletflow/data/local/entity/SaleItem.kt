package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sale_items",
    indices = [
        Index("transactionId"),
        Index("itemId"),
        Index("lotId"),
        Index("userId")
    ]
)
data class SaleItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val transactionId: String = "",
    val itemId: String = "",
    val lotId: String = "",
    val quantity: Int = 0,
    val sellingPrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
