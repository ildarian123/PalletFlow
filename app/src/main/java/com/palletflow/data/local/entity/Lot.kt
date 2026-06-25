package com.palletflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lots")
data class Lot(
    @PrimaryKey val id: Int
)