package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "warehouses",
    indices = [Index("userId")]
)
data class Warehouse(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val address: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

