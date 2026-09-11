package com.preciousmetals.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metal: String,
    val objectType: String,
    val grams: Double,
    val purchaseDateEpochDay: Long,
    val label: String,
    val pricePaidUsd: Double?,
    val photoUri: String?,
    val notes: String,
)
