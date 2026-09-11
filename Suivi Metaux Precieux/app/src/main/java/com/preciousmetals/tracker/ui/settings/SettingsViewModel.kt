package com.preciousmetals.tracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.export.DataExporter
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.domain.model.Currency
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val dataExporter: DataExporter,
    private val priceRepository: PriceRepository,
    private val onRefreshIntervalChanged: (Int) -> Unit,
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)
    private val isBackfilling = MutableStateFlow(false)

    val uiState = combine(
        userPreferences.displayCurrency,
        userPreferences.refreshIntervalMinutes,
        userPreferences.notificationsEnabled,
        userPreferences.lastRefreshEpochMillis,
        message,
        isBackfilling,
        userPreferences.appLockEnabled,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettingsUiState(
            currency = values[0] as Currency,
            refreshIntervalMinutes = values[1] as Int,
            notificationsEnabled = values[2] as Boolean,
            lastRefreshEpochMillis = values[3] as Long?,
            message = values[4] as String?,
            isBackfillingHistory = values[5] as Boolean,
            appLockEnabled = values[6] as Boolean,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { userPreferences.setDisplayCurrency(currency) }
    }

    fun setRefreshIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            userPreferences.setRefreshIntervalMinutes(minutes)
            onRefreshIntervalChanged(minutes)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setNotificationsEnabled(enabled) }
    }

    fun exportData(outputStream: OutputStream) {
        viewModelScope.launch {
            runCatching { dataExporter.exportToStream(outputStream) }
                .onSuccess { message.value = "Export réussi." }
                .onFailure { message.value = "Échec de l'export : ${it.message}" }
        }
    }

    fun exportDataCsv(outputStream: OutputStream) {
        viewModelScope.launch {
            runCatching { dataExporter.exportToCsvStream(outputStream) }
                .onSuccess { message.value = "Export CSV réussi." }
                .onFailure { message.value = "Échec de l'export CSV : ${it.message}" }
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAppLockEnabled(enabled) }
    }

    fun importData(inputStream: InputStream) {
        viewModelScope.launch {
            runCatching { dataExporter.importFromStream(inputStream) }
                .onSuccess { count -> message.value = "$count avoir(s) importé(s)." }
                .onFailure { message.value = "Échec de l'import : ${it.message}" }
        }
    }

    fun clearMessage() {
        message.value = null
    }

    fun backfillHistory() {
        viewModelScope.launch {
            isBackfilling.value = true
            priceRepository.backfillHistoricalPrices()
                .onSuccess {
                    userPreferences.setHistoricalBackfillDone(true)
                    message.value = "Historique des cours (5 ans) mis à jour."
                }
                .onFailure {
                    message.value = "Échec du rechargement de l'historique (réessayez plus tard)."
                }
            isBackfilling.value = false
        }
    }
}
