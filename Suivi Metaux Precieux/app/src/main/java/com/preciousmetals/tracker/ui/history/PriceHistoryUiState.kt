package com.preciousmetals.tracker.ui.history

import com.preciousmetals.tracker.data.repository.PricePoint
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal

sealed interface PriceHistoryUiState {
    data object Loading : PriceHistoryUiState

    data class Loaded(
        val metal: Metal,
        val rangeDays: Int,
        val history: List<PricePoint>,
        val latestPriceUsdPerGram: Double?,
        val currency: Currency,
        val usdToEurRate: Double,
    ) : PriceHistoryUiState
}
