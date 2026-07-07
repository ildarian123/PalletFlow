package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "lots",
    indices = [
        Index("userId"),
        Index("supplierId"),
        Index("warehouseId"),
        Index(value = ["lotNumber"], unique = true)
    ]
)
data class Lot(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val lotNumber: String,
    val supplierId: String,
    val warehouseId: String,
    val purchasePrice: Double,
    val status: LotStatus = LotStatus.DRAFT,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class LotStatus {
    DRAFT, COMPLETED, ARCHIVED
}
