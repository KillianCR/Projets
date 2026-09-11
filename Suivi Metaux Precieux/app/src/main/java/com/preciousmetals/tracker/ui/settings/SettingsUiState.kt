package com.preciousmetals.tracker.ui.settings

import com.preciousmetals.tracker.domain.model.Currency

data class SettingsUiState(
    val currency: Currency = Currency.EUR,
    val refreshIntervalMinutes: Int = 360,
    val notificationsEnabled: Boolean = true,
    val lastRefreshEpochMillis: Long? = null,
    val message: String? = null,
    val isBackfillingHistory: Boolean = false,
    val appLockEnabled: Boolean = false,
)
