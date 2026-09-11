package com.preciousmetals.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_locations")
data class StorageLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String,
    /** Optional reminder date (e.g. insurance renewal) for this location, null if none set. */
    val insuranceReminderEpochDay: Long?,
)
