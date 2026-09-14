package com.preciousmetals.tracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.PortfolioRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioHistoryViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val selectedRangeDays = MutableStateFlow(30)

    val uiState = selectedRangeDays
        .flatMapLatest { days ->
            combine(
                portfolioRepository.observeValueHistory(LocalDate.now().minusDays(days.toLong())),
                userPreferences.displayCurrency,
                userPreferences.usdToEurRate,
            ) { history, currency, rate ->
                PortfolioHistoryUiState.Loaded(
                    rangeDays = days,
                    history = history,
                    currentValueUsd = history.lastOrNull()?.totalValueUsd,
                    marketStats = marketStats(),
                    currency = currency,
                    usdToEurRate = rate,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PortfolioHistoryUiState.Loading,
        )

    fun selectRange(days: Int) {
        selectedRangeDays.value = days
    }

    /** Performance vs 1 week/1 month/3 months/1 year/5 years ago, same purchase-date-gated
     * point-in-time valuation as the chart itself (see [PortfolioRepository.getValueUsdAt]). */
    private suspend fun marketStats(): List<MarketStat> {
        val now = LocalDate.now()
        val currentValueUsd = portfolioRepository.getValueUsdAt(now)
        val lookbacks = listOf(7 to "1S", 30 to "1M", 90 to "3M", 365 to "1A", 1825 to "5A")
        return lookbacks.map { (daysAgo, label) ->
            val pastValueUsd = portfolioRepository.getValueUsdAt(now.minusDays(daysAgo.toLong()))
            val percentChange = if (pastValueUsd > 0.0) {
                ((currentValueUsd - pastValueUsd) / pastValueUsd) * 100.0
            } else {
                null
            }
            MarketStat(label, percentChange)
        }
    }
}
