package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.dao.PriceHistoryDao
import com.preciousmetals.tracker.data.local.entity.PriceHistoryEntity
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.remote.ExchangeRateApiService
import com.preciousmetals.tracker.data.remote.GoldApiService
import com.preciousmetals.tracker.data.remote.YahooFinanceApiService
import com.preciousmetals.tracker.domain.model.GRAMS_PER_TROY_OUNCE
import com.preciousmetals.tracker.domain.model.Metal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Fetches live spot prices and USD/EUR exchange rates, caches them locally (building a real
 * price-history table over time), and serves both live and historical lookups.
 *
 * Historical valuations for dates before this app was installed are covered by a one-time
 * [backfillHistoricalPrices] call (see [com.preciousmetals.tracker.SuiviMetauxApp]), which pulls
 * ~5 years of free daily closes from Yahoo Finance's public chart feed. That feed is free and
 * keyless but unofficial/undocumented, so it can occasionally fail or rate-limit — the backfill
 * is best-effort and silently retried on a later app launch if it didn't complete; the "prix
 * payé" manual entry always remains available as a fallback in the add/edit holding screen.
 */
class PriceRepository(
    private val goldApiService: GoldApiService,
    private val exchangeRateApiService: ExchangeRateApiService,
    private val yahooFinanceApiService: YahooFinanceApiService,
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

    /** Map of every tracked metal to its recent price history (USD/gram), for compact sparklines. */
    fun observeAllHistoryUsdPerGram(sinceDate: LocalDate): Flow<Map<Metal, List<PricePoint>>> {
        val perMetalFlows = Metal.entries.map { metal ->
            observeHistoryUsdPerGram(metal, sinceDate).map { metal to it }
        }
        return combine(perMetalFlows) { pairs -> pairs.toMap() }
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

    /**
     * One-time backfill of ~5 years of daily closes per metal, from Yahoo Finance's free,
     * keyless (but unofficial) chart feed. Safe to call repeatedly: rows are keyed by
     * (metal, day) and overwritten, not duplicated. See the class doc for the reliability
     * caveat — callers should treat failure as "try again later", not a fatal error.
     */
    suspend fun backfillHistoricalPrices(): Result<Unit> = runCatching {
        for (metal in Metal.entries) {
            val response = yahooFinanceApiService.getChart(metal.yahooSymbol, range = "5y", interval = "1d")
            val result = response.chart.result?.firstOrNull() ?: continue
            val closes = result.indicators.quote.firstOrNull()?.close ?: continue

            result.timestamp.forEachIndexed { index, epochSeconds ->
                val close = closes.getOrNull(index) ?: return@forEachIndexed
                val date = Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate()
                priceHistoryDao.insert(
                    PriceHistoryEntity(
                        metal = metal.name,
                        priceUsdPerOunce = close,
                        dateEpochDay = date.toEpochDay(),
                        timestampEpochMillis = epochSeconds * 1000,
                    )
                )
            }
        }
    }
}

data class PricePoint(val date: LocalDate, val priceUsdPerGram: Double)
