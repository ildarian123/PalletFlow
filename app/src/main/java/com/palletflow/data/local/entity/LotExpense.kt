package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "lot_expenses",
    foreignKeys = [
        ForeignKey(
            entity = Lot::class,
            parentColumns = ["id"],
            childColumns = ["lotId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index("lotId"),
        Index("userId")
    ]
)
data class LotExpense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val lotId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
