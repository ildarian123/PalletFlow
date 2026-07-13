package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "suppliers",
    indices = [Index("userId")]
)
data class Supplier(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val isFrequent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
