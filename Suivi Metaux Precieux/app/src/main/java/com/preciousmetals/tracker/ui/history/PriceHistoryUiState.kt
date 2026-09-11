package com.preciousmetals.tracker.ui.history

import com.preciousmetals.tracker.data.repository.PricePoint
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal

/** One "Performance 1S/1M/3M/1A"-style row; null when there isn't enough cached history yet. */
data class MarketStat(val label: String, val percentChange: Double?)

sealed interface PriceHistoryUiState {
    data object Loading : PriceHistoryUiState

    data class Loaded(
        val metal: Metal,
        val rangeDays: Int,
        val history: List<PricePoint>,
        val latestPriceUsdPerGram: Double?,
        val marketStats: List<MarketStat>,
        val currency: Currency,
        val usdToEurRate: Double,
    ) : PriceHistoryUiState
}
