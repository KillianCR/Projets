package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.domain.model.CostBasisSource
import com.preciousmetals.tracker.domain.model.HoldingValuation
import com.preciousmetals.tracker.domain.model.PortfolioSummary
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
}
