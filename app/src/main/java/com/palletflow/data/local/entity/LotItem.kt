package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "lot_items",
    foreignKeys = [
        ForeignKey(
            entity = Lot::class,
            parentColumns = ["id"],
            childColumns = ["lotId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("lotId"),
        Index("itemId"),
        Index("userId")
    ]
)
data class LotItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val lotId: String = "",
    val itemId: String = "",
    val quantity: Int = 0,
    val unitPrice: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
