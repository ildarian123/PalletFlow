package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val email: String = "",
    val passwordHash: String = "",
    val businessName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)