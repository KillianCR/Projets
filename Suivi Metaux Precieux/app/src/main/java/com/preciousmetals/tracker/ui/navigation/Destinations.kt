package com.preciousmetals.tracker.ui.navigation

import com.preciousmetals.tracker.domain.model.Metal

object Destinations {
    /** Hosts all 5 bottom tabs at once (see [TabHost]) — a single NavHost destination so
     * switching between them is a local state flip, not a real navigation transaction. */
    const val MAIN = "main"

    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val ALERTS = "alerts"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"

    const val HOLDING_ID_ARG = "holdingId"
    const val ADD_EDIT_HOLDING_PATTERN = "holding?holdingId={$HOLDING_ID_ARG}"

    const val HISTORY_METAL_ARG = "metal"
    const val HISTORY_FOR_METAL_PATTERN = "history/{$HISTORY_METAL_ARG}"

    const val LOCATION_ID_ARG = "locationId"
    // No location pre-selected (e.g. from the portfolio's "Stockage" quick action) falls back to
    // the first location the screen finds — see LocationDetailViewModel.
    const val LOCATION_DETAIL_PATTERN = "location?locationId={$LOCATION_ID_ARG}"

    fun addHolding(): String = "holding"
    fun editHolding(holdingId: Long): String = "holding?holdingId=$holdingId"
    fun historyForMetal(metal: Metal): String = "history/${metal.name}"
    fun locationDetail(locationId: Long? = null): String =
        if (locationId != null) "location?locationId=$locationId" else "location"
}
