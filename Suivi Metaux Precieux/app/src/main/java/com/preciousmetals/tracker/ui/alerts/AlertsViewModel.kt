package com.preciousmetals.tracker.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.AlertRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.domain.model.AlertDirection
import com.preciousmetals.tracker.domain.model.GRAMS_PER_TROY_OUNCE
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PriceAlert
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    ) { alerts, currency, rate -> AlertsUiState(alerts, currency, rate) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlertsUiState())

    fun addAlert(metal: Metal, direction: AlertDirection, thresholdEur: Double, perOunce: Boolean) {
        viewModelScope.launch {
            val rate = priceRepository.usdToEurRate.first()
            val thresholdEurPerGram = if (perOunce) thresholdEur / GRAMS_PER_TROY_OUNCE else thresholdEur
            val thresholdUsdPerGram = thresholdEurPerGram / rate
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
