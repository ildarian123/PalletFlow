package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lot_items")
data class LotItem(
    @PrimaryKey val id: Int
)