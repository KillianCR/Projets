package com.preciousmetals.tracker.domain.model

enum class AlertDirection { ABOVE, BELOW }

/** A user-defined threshold on a metal's spot price, expressed in USD per gram internally. */
data class PriceAlert(
    val id: Long = 0,
    val metal: Metal,
    val direction: AlertDirection,
    val thresholdUsdPerGram: Double,
    val enabled: Boolean,
    val createdAtEpochMillis: Long,
    val lastTriggeredAtEpochMillis: Long?,
)
