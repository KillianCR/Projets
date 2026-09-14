package com.preciousmetals.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metal: String,
    /** ABOVE or BELOW */
    val direction: String,
    val thresholdUsdPerGram: Double,
    /** EUR or USD — how this alert's own threshold displays, independent of the app's global
     * display currency. See MIGRATION_3_4 for the default given to alerts that predate this column. */
    val currency: String,
    val enabled: Boolean,
    val createdAtEpochMillis: Long,
    val lastTriggeredAtEpochMillis: Long?,
)
