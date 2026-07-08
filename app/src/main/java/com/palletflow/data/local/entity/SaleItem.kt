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
        Index("lotId")
    ]
)
data class SaleItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionId: String,
    val itemId: String,
    val lotId: String,
    val quantity: Int,
    val sellingPrice: Double,
    val createdAt: Long = System.currentTimeMillis()
)
