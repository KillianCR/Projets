package com.preciousmetals.tracker.domain.model

enum class AlertDirection { ABOVE, BELOW }

/**
 * A user-defined threshold on a metal's spot price. [thresholdUsdPerGram] is always stored (and
 * compared against the live spot price) in USD per gram, regardless of [currency] — [currency] is
 * purely how this one alert's threshold is displayed, independent of the app's global display
 * currency setting, so an alert created in EUR keeps reading in EUR even if the user later
 * switches the app itself to USD (or vice versa).
 */
data class PriceAlert(
    val id: Long = 0,
    val metal: Metal,
    val direction: AlertDirection,
    val thresholdUsdPerGram: Double,
    val currency: Currency,
    val enabled: Boolean,
    val createdAtEpochMillis: Long,
    val lastTriggeredAtEpochMillis: Long?,
)
