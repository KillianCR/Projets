package com.preciousmetals.tracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.PortfolioRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PortfolioSummary
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val priceRepository: PriceRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<String?>(null)

    private data class RefreshState(val isRefreshing: Boolean, val error: String?)
    private data class PortfolioAndPrices(
        val summary: PortfolioSummary,
        val livePrices: Map<Metal, Double?>,
        val sparklines: Map<Metal, List<Double>>,
    )
    private data class DisplayPrefs(val currency: Currency, val rate: Double, val refresh: RefreshState)

    private val refreshState = combine(isRefreshing, refreshError, ::RefreshState)

    private val portfolioAndPrices = combine(
        portfolioRepository.observePortfolio(),
        priceRepository.observeAllLatestPricesUsdPerGram(),
        priceRepository.observeAllHistoryUsdPerGram(LocalDate.now().minusDays(7)),
    ) { summary, livePrices, history ->
        PortfolioAndPrices(
            summary = summary,
            livePrices = livePrices,
            sparklines = history.mapValues { (_, points) -> points.map { it.priceUsdPerGram } },
        )
    }

    private val displayPrefs = combine(
        userPreferences.displayCurrency,
        priceRepository.usdToEurRate,
        refreshState,
        ::DisplayPrefs,
    )

    val uiState = combine(portfolioAndPrices, displayPrefs) { pp, prefs ->
        DashboardUiState.Loaded(
            summary = pp.summary,
            livePricesUsdPerGram = pp.livePrices,
            sparklineByMetal = pp.sparklines,
            currency = prefs.currency,
            usdToEurRate = prefs.rate,
            isRefreshing = prefs.refresh.isRefreshing,
            refreshError = prefs.refresh.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            val result = priceRepository.refreshAll()
            refreshError.value = result.exceptionOrNull()?.let {
                it.message ?: "Impossible de récupérer les cours (vérifiez votre connexion)."
            }
            isRefreshing.value = false
        }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { userPreferences.setDisplayCurrency(currency) }
    }
}
