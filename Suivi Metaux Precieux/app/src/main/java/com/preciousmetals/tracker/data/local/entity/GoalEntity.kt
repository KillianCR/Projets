package com.preciousmetals.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    /** Metal enum name, or null when the goal targets total portfolio value across all metals. */
    val metal: String?,
    /** "WEIGHT_GRAMS" or "VALUE_USD" — see domain.model.GoalTargetType. */
    val targetType: String,
    val targetAmount: Double,
    val createdAtEpochMillis: Long,
)
