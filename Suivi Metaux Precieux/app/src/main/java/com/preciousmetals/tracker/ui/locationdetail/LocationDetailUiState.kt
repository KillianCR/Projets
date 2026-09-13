package com.preciousmetals.tracker.ui.locationdetail

import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.HoldingValuation
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.StorageLocation

/** One metal's aggregated position within a single storage location. */
data class MetalLocationSummary(
    val metal: Metal,
    val totalValueUsd: Double,
    val totalGrams: Double,
)

sealed interface LocationDetailUiState {
    data object Loading : LocationDetailUiState

    data class Loaded(
        /** Every storage location, for the picker row — lets you switch which one you're viewing
         * without leaving the screen. */
        val locations: List<StorageLocation>,
        val selectedLocationId: Long?,
        val locationName: String,
        val valuations: List<HoldingValuation>,
        val totalValueUsd: Double,
        val byMetal: List<MetalLocationSummary>,
        val currency: Currency,
        val usdToEurRate: Double,
    ) : LocationDetailUiState
}
