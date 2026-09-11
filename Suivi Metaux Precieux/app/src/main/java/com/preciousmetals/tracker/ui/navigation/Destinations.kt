package com.preciousmetals.tracker.ui.navigation

object Destinations {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val ALERTS = "alerts"
    const val SETTINGS = "settings"

    const val HOLDING_ID_ARG = "holdingId"
    const val ADD_EDIT_HOLDING_PATTERN = "holding?holdingId={$HOLDING_ID_ARG}"

    fun addHolding(): String = "holding"
    fun editHolding(holdingId: Long): String = "holding?holdingId=$holdingId"
}

data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
