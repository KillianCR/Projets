package com.preciousmetals.tracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.domain.model.Metal
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class PriceHistoryViewModel(
    private val priceRepository: PriceRepository,
    private val userPreferences: UserPreferences,
    initialMetal: Metal = Metal.GOLD,
) : ViewModel() {

    private val selectedMetal = MutableStateFlow(initialMetal)
    private val selectedRangeDays = MutableStateFlow(30)

    val uiState = combine(selectedMetal, selectedRangeDays) { metal, days -> metal to days }
        .flatMapLatest { (metal, days) ->
            combine(
                priceRepository.observeHistoryUsdPerGram(metal, LocalDate.now().minusDays(days.toLong())),
                priceRepository.observeLatestPriceUsdPerGram(metal),
                userPreferences.displayCurrency,
                priceRepository.usdToEurRate,
            ) { history, latest, currency, rate ->
                PriceHistoryUiState.Loaded(
                    metal = metal,
                    rangeDays = days,
                    history = history,
                    latestPriceUsdPerGram = latest,
                    marketStats = marketStats(metal, latest),
                    currency = currency,
                    usdToEurRate = rate,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PriceHistoryUiState.Loading,
        )

    fun selectMetal(metal: Metal) {
        selectedMetal.value = metal
    }

    fun selectRange(days: Int) {
        selectedRangeDays.value = days
    }

    /** Performance vs 1 week/1 month/3 months/1 year ago, from the cached daily-close history. */
    private suspend fun marketStats(metal: Metal, latest: Double?): List<MarketStat> {
        val lookbacks = listOf(7 to "1S", 30 to "1M", 90 to "3M", 365 to "1A")
        return lookbacks.map { (daysAgo, label) ->
            val percentChange = if (latest != null) {
                val past = priceRepository.getNearestHistoricalPriceUsdPerGram(metal, LocalDate.now().minusDays(daysAgo.toLong()))
                if (past != null && past > 0.0) ((latest - past) / past) * 100.0 else null
            } else {
                null
            }
            MarketStat(label, percentChange)
        }
    }
}
