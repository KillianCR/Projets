package com.preciousmetals.tracker.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.AlertRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.domain.model.AlertDirection
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PriceAlert
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val alertRepository: AlertRepository,
    private val priceRepository: PriceRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val uiState = combine(
        alertRepository.observeAll(),
        userPreferences.displayCurrency,
        priceRepository.usdToEurRate,
        priceRepository.observeAllLatestPricesUsdPerGram(),
    ) { alerts, currency, rate, livePrices -> AlertsUiState(alerts, currency, rate, livePrices) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlertsUiState())

    /** Threshold expressed relative to [metal]'s current cached price — e.g. "+5% au-dessus". */
    fun addAlertByPercent(metal: Metal, direction: AlertDirection, percent: Double) {
        viewModelScope.launch {
            val currentPriceUsdPerGram = priceRepository.getLatestPriceOnceUsdPerGram(metal) ?: return@launch
            val sign = if (direction == AlertDirection.ABOVE) 1.0 else -1.0
            val thresholdUsdPerGram = currentPriceUsdPerGram * (1.0 + sign * percent / 100.0)
            alertRepository.upsert(
                PriceAlert(
                    metal = metal,
                    direction = direction,
                    thresholdUsdPerGram = thresholdUsdPerGram,
                    enabled = true,
                    createdAtEpochMillis = System.currentTimeMillis(),
                    lastTriggeredAtEpochMillis = null,
                )
            )
        }
    }

    fun deleteAlert(alert: PriceAlert) {
        viewModelScope.launch { alertRepository.delete(alert) }
    }

    fun toggleAlert(alert: PriceAlert) {
        viewModelScope.launch { alertRepository.upsert(alert.copy(enabled = !alert.enabled)) }
    }
}
