package com.preciousmetals.tracker.domain.model

enum class GoalTargetType { WEIGHT_GRAMS, VALUE_USD }

/**
 * A savings-goal — either a weight of a specific [metal], or a total portfolio value across all
 * metals (when [metal] is null). Progress is computed live from the current portfolio, not
 * stored, so it always reflects the latest spot prices/holdings.
 */
data class Goal(
    val id: Long = 0,
    val label: String,
    val metal: Metal?,
    val targetType: GoalTargetType,
    val targetAmount: Double,
    val createdAtEpochMillis: Long,
)
