package com.preciousmetals.tracker.ui.dashboard

import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PortfolioSummary

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Loaded(
        val summary: PortfolioSummary,
        /** Latest spot price per metal, USD/gram, null while a metal's price hasn't loaded yet. */
        val livePricesUsdPerGram: Map<Metal, Double?>,
        /** Last ~7 days of price/gram per metal, for the ticker sparklines. */
        val sparklineByMetal: Map<Metal, List<Double>>,
        val currency: Currency,
        val usdToEurRate: Double,
        val isRefreshing: Boolean,
        val refreshError: String?,
    ) : DashboardUiState
}
