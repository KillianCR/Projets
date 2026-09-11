package com.preciousmetals.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One cached spot price sample, in USD per troy ounce, for a given metal. */
@Entity(tableName = "price_history")
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metal: String,
    val priceUsdPerOunce: Double,
    val dateEpochDay: Long,
    val timestampEpochMillis: Long,
)
