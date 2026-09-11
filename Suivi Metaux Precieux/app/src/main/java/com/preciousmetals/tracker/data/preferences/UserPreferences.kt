package com.preciousmetals.tracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.preciousmetals.tracker.domain.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

/** Fallback USD→EUR rate used only until the first successful network refresh. */
private const val FALLBACK_USD_TO_EUR_RATE = 0.92

class UserPreferences(private val context: Context) {

    private object Keys {
        val DISPLAY_CURRENCY = stringPreferencesKey("display_currency")
        val REFRESH_INTERVAL_MINUTES = intPreferencesKey("refresh_interval_minutes")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val USD_TO_EUR_RATE = doublePreferencesKey("usd_to_eur_rate")
        val LAST_REFRESH_EPOCH_MILLIS = longPreferencesKey("last_refresh_epoch_millis")
    }

    val displayCurrency: Flow<Currency> = context.dataStore.data.map { prefs ->
        prefs[Keys.DISPLAY_CURRENCY]?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
            ?: Currency.EUR
    }

    suspend fun setDisplayCurrency(currency: Currency) {
        context.dataStore.edit { it[Keys.DISPLAY_CURRENCY] = currency.name }
    }

    val refreshIntervalMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.REFRESH_INTERVAL_MINUTES] ?: 360
    }

    suspend fun setRefreshIntervalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.REFRESH_INTERVAL_MINUTES] = minutes }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    val usdToEurRate: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[Keys.USD_TO_EUR_RATE] ?: FALLBACK_USD_TO_EUR_RATE
    }

    suspend fun setUsdToEurRate(rate: Double) {
        context.dataStore.edit { it[Keys.USD_TO_EUR_RATE] = rate }
    }

    val lastRefreshEpochMillis: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_REFRESH_EPOCH_MILLIS]
    }

    suspend fun setLastRefreshEpochMillis(epochMillis: Long) {
        context.dataStore.edit { it[Keys.LAST_REFRESH_EPOCH_MILLIS] = epochMillis }
    }
}
