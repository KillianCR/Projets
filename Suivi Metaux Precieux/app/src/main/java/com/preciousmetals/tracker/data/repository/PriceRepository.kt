package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.dao.PriceHistoryDao
import com.preciousmetals.tracker.data.local.entity.PriceHistoryEntity
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.remote.ExchangeRateApiService
import com.preciousmetals.tracker.data.remote.GoldApiService
import com.preciousmetals.tracker.domain.model.GRAMS_PER_TROY_OUNCE
import com.preciousmetals.tracker.domain.model.Metal
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Fetches live spot prices and USD/EUR exchange rates, caches them locally (building a real
 * price-history table over time), and serves both live and historical lookups.
 *
 * Historical valuations only cover dates on or after this app's own first refresh: there is no
 * free, keyless provider of past gold/silver/platinum/palladium prices, so a purchase dated
 * before the app started tracking has no automatically derivable cost basis (see
 * [getNearestHistoricalPriceUsdPerGram]) and the user is asked to enter the price they paid.
 */
class PriceRepository(
    private val goldApiService: GoldApiService,
    private val exchangeRateApiService: ExchangeRateApiService,
    private val priceHistoryDao: PriceHistoryDao,
    private val userPreferences: UserPreferences,
) {

    fun observeLatestPriceUsdPerGram(metal: Metal): Flow<Double?> =
        priceHistoryDao.observeLatest(metal.name).map { it?.priceUsdPerOunce?.div(GRAMS_PER_TROY_OUNCE) }

    /** Map of every tracked metal to its latest cached price (USD/gram), null while unknown. */
    fun observeAllLatestPricesUsdPerGram(): Flow<Map<Metal, Double?>> {
        val perMetalFlows = Metal.entries.map { metal ->
            observeLatestPriceUsdPerGram(metal).map { metal to it }
        }
        return combine(perMetalFlows) { pairs -> pairs.toMap() }
    }

    fun observeHistoryUsdPerGram(metal: Metal, sinceDate: LocalDate): Flow<List<PricePoint>> =
        priceHistoryDao.observeHistorySince(metal.name, sinceDate.toEpochDay()).map { entities ->
            entities.map { PricePoint(LocalDate.ofEpochDay(it.dateEpochDay), it.priceUsdPerOunce / GRAMS_PER_TROY_OUNCE) }
        }

    /** Nearest cached sample at or before [date], or null if no cached price reaches back that far. */
    suspend fun getNearestHistoricalPriceUsdPerGram(metal: Metal, date: LocalDate): Double? =
        priceHistoryDao.getNearestOnOrBefore(metal.name, date.toEpochDay())
            ?.let { it.priceUsdPerOunce / GRAMS_PER_TROY_OUNCE }

    suspend fun getLatestPriceOnceUsdPerGram(metal: Metal): Double? =
        priceHistoryDao.getLatestOnce(metal.name)?.let { it.priceUsdPerOunce / GRAMS_PER_TROY_OUNCE }

    val usdToEurRate: Flow<Double> = userPreferences.usdToEurRate

    suspend fun refreshAll(): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val today = LocalDate.now().toEpochDay()

        for (metal in Metal.entries) {
            val dto = goldApiService.getSpotPrice(metal.apiSymbol)
            priceHistoryDao.insert(
                PriceHistoryEntity(
                    metal = metal.name,
                    priceUsdPerOunce = dto.price,
                    dateEpochDay = today,
                    timestampEpochMillis = now,
                )
            )
        }

        val rateDto = exchangeRateApiService.getLatest(from = "USD", to = "EUR")
        rateDto.rates["EUR"]?.let { userPreferences.setUsdToEurRate(it) }

        userPreferences.setLastRefreshEpochMillis(now)
    }
}

data class PricePoint(val date: LocalDate, val priceUsdPerGram: Double)
