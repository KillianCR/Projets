package com.preciousmetals.tracker.ui.tools

import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Goal
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.StorageLocation

/** [currentAmount] and [progressFraction] are computed live from the portfolio, never stored. */
data class GoalProgress(
    val goal: Goal,
    val currentAmount: Double,
    val progressFraction: Float,
)

data class ToolsUiState(
    val goals: List<GoalProgress> = emptyList(),
    val storageLocations: List<StorageLocation> = emptyList(),
    val holdingCountByLocationId: Map<Long, Int> = emptyMap(),
    val currency: Currency = Currency.EUR,
    val usdToEurRate: Double = 1.0,
    val livePricesUsdPerGram: Map<Metal, Double?> = emptyMap(),
)
