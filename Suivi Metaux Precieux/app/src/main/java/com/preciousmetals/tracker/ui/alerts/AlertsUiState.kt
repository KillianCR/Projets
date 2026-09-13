package com.preciousmetals.tracker.ui.alerts

import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PriceAlert

data class AlertsUiState(
    val alerts: List<PriceAlert> = emptyList(),
    val currency: Currency = Currency.EUR,
    val usdToEurRate: Double = 1.0,
    val livePricesUsdPerGram: Map<Metal, Double?> = emptyMap(),
)
