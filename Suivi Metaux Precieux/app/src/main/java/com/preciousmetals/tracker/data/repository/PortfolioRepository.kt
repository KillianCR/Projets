package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.domain.model.CostBasisSource
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.HoldingValuation
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PortfolioSummary
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Joins holdings with live prices (and, when needed, cached historical prices) into a portfolio view. */
class PortfolioRepository(
    private val holdingRepository: HoldingRepository,
    private val priceRepository: PriceRepository,
) {

    fun observePortfolio(): Flow<PortfolioSummary> = combine(
        holdingRepository.observeAll(),
        priceRepository.observeAllLatestPricesUsdPerGram(),
    ) { holdings, latestPrices ->
        val valuations = holdings.map { holding ->
            val livePricePerGram = latestPrices[holding.metal]
            val currentValueUsd = (livePricePerGram ?: 0.0) * holding.grams

            val (costBasisUsd, source) = when {
                holding.pricePaidUsd != null ->
                    holding.pricePaidUsd to CostBasisSource.MANUAL_PRICE

                else -> {
                    val historicalPricePerGram = priceRepository
                        .getNearestHistoricalPriceUsdPerGram(holding.metal, holding.purchaseDate)
                    if (historicalPricePerGram != null) {
                        (historicalPricePerGram * holding.grams) to CostBasisSource.HISTORICAL_SPOT
                    } else {
                        null to CostBasisSource.UNAVAILABLE
                    }
                }
            }

            HoldingValuation(
                holding = holding,
                currentValueUsd = currentValueUsd,
                hasLivePrice = livePricePerGram != null,
                costBasisUsd = costBasisUsd,
                costBasisSource = source,
            )
        }
        PortfolioSummary(valuations)
    }

    /**
     * The portfolio's total USD value on each day a cached price sample exists since [sinceDate],
     * for the "Historique du portefeuille" chart. A holding only contributes on/after its own
     * [Holding.purchaseDate] — the whole point of this series is that it must NOT show a value for
     * a quantity of metal that wasn't actually in the portfolio yet on that date.
     */
    fun observeValueHistory(sinceDate: LocalDate): Flow<List<PortfolioValuePoint>> = combine(
        holdingRepository.observeAll(),
        priceRepository.observeAllHistoryUsdPerGram(sinceDate),
    ) { holdings, historyByMetal -> computeValueHistory(holdings, historyByMetal) }

    /**
     * One-shot total USD value as of [date], using the nearest cached price at or before it per
     * metal — same purchase-date gate as [observeValueHistory]. Used for the screen's "Performance
     * 1S/1M/3M/1A/5A" stats, which need a single point-in-time value rather than a full series.
     */
    suspend fun getValueUsdAt(date: LocalDate): Double {
        val holdings = holdingRepository.getAllOnce()
        var total = 0.0
        for (holding in holdings) {
            if (holding.purchaseDate.isAfter(date)) continue
            val price = priceRepository.getNearestHistoricalPriceUsdPerGram(holding.metal, date) ?: continue
            total += price * holding.grams
        }
        return total
    }

    /**
     * Walks each held metal's price series once (not one DB query per day per holding) to build
     * one [PortfolioValuePoint] per date any held metal has a cached sample for. Each metal's
     * series in [historyByMetal] is already sorted ascending by date (see
     * [PriceRepository.observeHistoryUsdPerGram]), so a single advancing index per metal is enough
     * to track "latest known price at or before the current date" while scanning dates in order.
     *
     * [PortfolioValuePoint.totalCostBasisUsd] is what turns the value series into a real
     * performance chart: each holding's cost basis is fixed at purchase (same manual-price-or-
     * historical-spot logic as [observePortfolio]) and computed once per holding, not once per day
     * — only which holdings have been bought yet changes as the date advances, not what they cost.
     * Without this, a day where a holding is simply added would otherwise read as a price gain
     * equal to its whole value, since there'd be nothing to net it against.
     */
    private suspend fun computeValueHistory(
        holdings: List<Holding>,
        historyByMetal: Map<Metal, List<PricePoint>>,
    ): List<PortfolioValuePoint> {
        if (holdings.isEmpty()) return emptyList()
        val heldMetals = holdings.mapTo(mutableSetOf()) { it.metal }
        val relevantHistory = historyByMetal.filterKeys { it in heldMetals }
        val allDates = relevantHistory.values.flatMapTo(sortedSetOf()) { series -> series.map { it.date } }
        if (allDates.isEmpty()) return emptyList()

        val costBasisUsdByHolding = holdings.associateWith { holding ->
            holding.pricePaidUsd ?: run {
                val historicalPricePerGram = priceRepository
                    .getNearestHistoricalPriceUsdPerGram(holding.metal, holding.purchaseDate)
                (historicalPricePerGram ?: 0.0) * holding.grams
            }
        }

        val nextIndex = HashMap<Metal, Int>(heldMetals.size)
        val lastKnownPriceUsdPerGram = HashMap<Metal, Double>(heldMetals.size)
        val points = ArrayList<PortfolioValuePoint>(allDates.size)
        for (date in allDates) {
            for (metal in heldMetals) {
                val series = relevantHistory[metal] ?: continue
                var index = nextIndex.getOrDefault(metal, 0)
                while (index < series.size && !series[index].date.isAfter(date)) {
                    lastKnownPriceUsdPerGram[metal] = series[index].priceUsdPerGram
                    index++
                }
                nextIndex[metal] = index
            }

            var totalUsd = 0.0
            var totalCostBasisUsd = 0.0
            for (holding in holdings) {
                if (holding.purchaseDate.isAfter(date)) continue
                val price = lastKnownPriceUsdPerGram[holding.metal] ?: continue
                totalUsd += price * holding.grams
                totalCostBasisUsd += costBasisUsdByHolding.getValue(holding)
            }
            points += PortfolioValuePoint(date, totalUsd, totalCostBasisUsd)
        }
        return points
    }
}

data class PortfolioValuePoint(val date: LocalDate, val totalValueUsd: Double, val totalCostBasisUsd: Double)
