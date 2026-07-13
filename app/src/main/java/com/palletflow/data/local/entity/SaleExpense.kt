package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sale_expenses",
    indices = [Index("transactionId")]
)
data class SaleExpense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
