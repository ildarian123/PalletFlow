package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val contactName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
