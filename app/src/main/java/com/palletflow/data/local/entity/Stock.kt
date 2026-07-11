package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "stocks",
    indices = [
        Index("userId"),
        Index("itemId"),
        Index("warehouseId"),
        Index(value = ["userId", "itemId", "warehouseId"], unique = true)
    ]
)
data class Stock(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val itemId: String,
    val warehouseId: String,
    val quantity: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
