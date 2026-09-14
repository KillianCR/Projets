package com.preciousmetals.tracker.ui.history

import com.preciousmetals.tracker.data.repository.PortfolioValuePoint
import com.preciousmetals.tracker.domain.model.Currency

sealed interface PortfolioHistoryUiState {
    data object Loading : PortfolioHistoryUiState

    data class Loaded(
        val rangeDays: Int,
        val history: List<PortfolioValuePoint>,
        val currentValueUsd: Double?,
        val marketStats: List<MarketStat>,
        val currency: Currency,
        val usdToEurRate: Double,
    ) : PortfolioHistoryUiState
}
