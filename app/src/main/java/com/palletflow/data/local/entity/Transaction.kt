package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    indices = [
        Index("userId"),
        Index("itemId"),
        Index("lotId"),
        Index("type")
    ]
)
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val type: TransactionType = TransactionType.SALE,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val itemId: String? = null,
    val lotId: String? = null,
    val quantity: Int = 0,
    val amount: Double = 0.0,
    val warehouseFromId: String? = null,
    val warehouseToId: String? = null,
    val platform: String? = null,
    val documentNumber: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOMING, SALE, TRANSFER
}

enum class TransactionStatus {
    DRAFT, COMPLETED, ARCHIVED
}
