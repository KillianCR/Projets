package com.preciousmetals.tracker.ui.dashboard

import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.PortfolioSummary

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Loaded(
        val summary: PortfolioSummary,
        val currency: Currency,
        val usdToEurRate: Double,
        val isRefreshing: Boolean,
        val refreshError: String?,
    ) : DashboardUiState
}
