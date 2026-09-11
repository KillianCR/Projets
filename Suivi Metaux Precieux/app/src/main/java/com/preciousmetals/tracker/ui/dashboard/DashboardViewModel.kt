package com.preciousmetals.tracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.HoldingRepository
import com.preciousmetals.tracker.data.repository.PortfolioRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val priceRepository: PriceRepository,
    private val userPreferences: UserPreferences,
    private val holdingRepository: HoldingRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<String?>(null)

    val uiState = combine(
        portfolioRepository.observePortfolio(),
        userPreferences.displayCurrency,
        priceRepository.usdToEurRate,
        isRefreshing,
        refreshError,
    ) { summary: PortfolioSummary, currency: Currency, rate: Double, refreshing: Boolean, error: String? ->
        DashboardUiState.Loaded(
            summary = summary,
            currency = currency,
            usdToEurRate = rate,
            isRefreshing = refreshing,
            refreshError = error,
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
                "Impossible de récupérer les cours (vérifiez votre connexion)."
            }
            isRefreshing.value = false
        }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { userPreferences.setDisplayCurrency(currency) }
    }

    fun deleteHolding(holding: Holding) {
        viewModelScope.launch { holdingRepository.delete(holding) }
    }
}
