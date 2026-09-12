package com.preciousmetals.tracker.ui.navigation

import com.preciousmetals.tracker.domain.model.Metal

object Destinations {
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
    const val LOCATION_DETAIL_PATTERN = "location/{$LOCATION_ID_ARG}"

    fun addHolding(): String = "holding"
    fun editHolding(holdingId: Long): String = "holding?holdingId=$holdingId"
    fun historyForMetal(metal: Metal): String = "history/${metal.name}"
    fun locationDetail(locationId: Long): String = "location/$locationId"
}
