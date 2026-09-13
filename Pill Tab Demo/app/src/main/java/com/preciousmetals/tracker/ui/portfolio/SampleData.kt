package com.preciousmetals.tracker.ui.portfolio

import com.preciousmetals.tracker.domain.model.CostBasisSource
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.HoldingValuation
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.ObjectType
import com.preciousmetals.tracker.domain.model.PortfolioSummary
import java.time.LocalDate

/** Static stand-in for what the real app loads from Room/the price API — just enough to drive the design. */
fun sampleLivePricesUsdPerGram(): Map<Metal, Double?> = mapOf(
    Metal.GOLD to 92.30,
    Metal.SILVER to 1.05,
    Metal.PLATINUM to 32.00,
    Metal.PALLADIUM to 34.10,
    Metal.COPPER to 0.01047,
)

/** A plausible 7-point wiggle around each metal's current price, for the ticker's sparkline. */
fun sampleSparklineByMetal(): Map<Metal, List<Double>> {
    val prices = sampleLivePricesUsdPerGram()
    val wiggle = listOf(-0.02, -0.01, 0.005, -0.015, 0.02, 0.01, 0.0)
    return prices.mapValues { (_, price) ->
        val base = price ?: 0.0
        wiggle.runningFold(base) { acc, delta -> acc * (1.0 + delta) }.drop(1)
    }
}

fun samplePortfolioSummary(): PortfolioSummary = PortfolioSummary(
    valuations = listOf(
        HoldingValuation(
            holding = Holding(
                id = 1,
                metal = Metal.GOLD,
                objectType = ObjectType.BAR,
                grams = 100.0,
                purchaseDate = LocalDate.of(2024, 3, 10),
                label = "Lingot 100g",
                pricePaidUsd = 8700.0,
                photoUri = null,
                notes = "",
            ),
            currentValueUsd = 9230.0,
            hasLivePrice = true,
            costBasisUsd = 8700.0,
            costBasisSource = CostBasisSource.MANUAL_PRICE,
        ),
        HoldingValuation(
            holding = Holding(
                id = 2,
                metal = Metal.GOLD,
                objectType = ObjectType.COIN,
                grams = 6.45,
                purchaseDate = LocalDate.of(2023, 11, 2),
                label = "Napoléon 20F",
                pricePaidUsd = 610.0,
                photoUri = null,
                notes = "",
            ),
            currentValueUsd = 590.0,
            hasLivePrice = true,
            costBasisUsd = 610.0,
            costBasisSource = CostBasisSource.MANUAL_PRICE,
        ),
        HoldingValuation(
            holding = Holding(
                id = 3,
                metal = Metal.SILVER,
                objectType = ObjectType.BAR,
                grams = 1000.0,
                purchaseDate = LocalDate.of(2024, 6, 20),
                label = "Lingot 1kg",
                pricePaidUsd = 950.0,
                photoUri = null,
                notes = "",
            ),
            currentValueUsd = 1050.0,
            hasLivePrice = true,
            costBasisUsd = 950.0,
            costBasisSource = CostBasisSource.MANUAL_PRICE,
        ),
        HoldingValuation(
            holding = Holding(
                id = 4,
                metal = Metal.PLATINUM,
                objectType = ObjectType.COIN,
                grams = 31.1,
                purchaseDate = LocalDate.of(2023, 5, 15),
                label = "Platinum Eagle",
                pricePaidUsd = 1040.0,
                photoUri = null,
                notes = "",
            ),
            currentValueUsd = 995.0,
            hasLivePrice = true,
            costBasisUsd = 1040.0,
            costBasisSource = CostBasisSource.MANUAL_PRICE,
        ),
        HoldingValuation(
            holding = Holding(
                id = 5,
                metal = Metal.PALLADIUM,
                objectType = ObjectType.BAR,
                grams = 50.0,
                purchaseDate = LocalDate.of(2022, 9, 1),
                label = "Lingot 50g",
                pricePaidUsd = 1500.0,
                photoUri = null,
                notes = "",
            ),
            currentValueUsd = 1705.0,
            hasLivePrice = true,
            costBasisUsd = 1500.0,
            costBasisSource = CostBasisSource.MANUAL_PRICE,
        ),
        HoldingValuation(
            holding = Holding(
                id = 6,
                metal = Metal.COPPER,
                objectType = ObjectType.OTHER,
                grams = 5000.0,
                purchaseDate = LocalDate.of(2024, 1, 12),
                label = "Lingot 5kg",
                pricePaidUsd = 60.0,
                photoUri = null,
                notes = "",
            ),
            currentValueUsd = 52.35,
            hasLivePrice = true,
            costBasisUsd = 60.0,
            costBasisSource = CostBasisSource.MANUAL_PRICE,
        ),
    ),
)
