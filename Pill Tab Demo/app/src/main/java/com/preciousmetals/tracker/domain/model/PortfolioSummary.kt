package com.preciousmetals.tracker.domain.model

/** Source used to establish a holding's acquisition cost. */
enum class CostBasisSource {
    MANUAL_PRICE,
    HISTORICAL_SPOT,
    UNAVAILABLE,
}

/** Computed valuation of a single [Holding], in USD (the app's internal storage currency). */
data class HoldingValuation(
    val holding: Holding,
    val currentValueUsd: Double,
    val hasLivePrice: Boolean,
    val costBasisUsd: Double?,
    val costBasisSource: CostBasisSource,
) {
    val gainLossUsd: Double?
        get() = costBasisUsd?.let { currentValueUsd - it }

    val gainLossPercent: Double?
        get() = costBasisUsd?.takeIf { it > 0.0 }?.let { basis ->
            ((currentValueUsd - basis) / basis) * 100.0
        }
}

/** Aggregated portfolio totals plus a per-metal breakdown, in USD. */
data class PortfolioSummary(
    val valuations: List<HoldingValuation>,
) {
    val totalValueUsd: Double get() = valuations.sumOf { it.currentValueUsd }

    val totalCostBasisUsd: Double? get() {
        val known = valuations.mapNotNull { it.costBasisUsd }
        return if (known.isEmpty()) null else known.sum()
    }

    val totalGainLossUsd: Double?
        get() = totalCostBasisUsd?.let { totalValueUsd - it }

    val totalGainLossPercent: Double?
        get() = totalCostBasisUsd?.takeIf { it > 0.0 }?.let { basis ->
            ((totalValueUsd - basis) / basis) * 100.0
        }

    val byMetal: Map<Metal, Double> get() =
        valuations.groupBy { it.holding.metal }
            .mapValues { (_, list) -> list.sumOf { it.currentValueUsd } }

    val byObjectType: Map<ObjectType, Double> get() =
        valuations.groupBy { it.holding.objectType }
            .mapValues { (_, list) -> list.sumOf { it.currentValueUsd } }
}
