package com.preciousmetals.tracker.ui.locationdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.PortfolioRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.data.repository.StorageLocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LocationDetailViewModel(
    initialLocationId: Long?,
    portfolioRepository: PortfolioRepository,
    storageLocationRepository: StorageLocationRepository,
    userPreferences: UserPreferences,
    priceRepository: PriceRepository,
) : ViewModel() {

    private val selectedLocationId = MutableStateFlow(initialLocationId)

    val uiState = combine(
        storageLocationRepository.observeAll(),
        portfolioRepository.observePortfolio(),
        userPreferences.displayCurrency,
        priceRepository.usdToEurRate,
        selectedLocationId,
    ) { locations, summary, currency, rate, selectedId ->
        // Falls back to the first location when nothing was pre-selected (the portfolio's
        // "Stockage" quick action) or the selected one no longer exists (deleted elsewhere).
        val resolvedId = selectedId?.takeIf { id -> locations.any { it.id == id } }
            ?: locations.firstOrNull()?.id
        val locationName = locations.firstOrNull { it.id == resolvedId }?.name ?: "Aucun lieu"
        val valuations = summary.valuations
            .filter { it.holding.storageLocationId == resolvedId }
            .sortedByDescending { it.holding.purchaseDate }
        val byMetal = valuations
            .groupBy { it.holding.metal }
            .map { (metal, list) ->
                MetalLocationSummary(
                    metal = metal,
                    totalValueUsd = list.sumOf { it.currentValueUsd },
                    totalGrams = list.sumOf { it.holding.grams },
                )
            }
            .sortedByDescending { it.totalValueUsd }

        LocationDetailUiState.Loaded(
            locations = locations,
            selectedLocationId = resolvedId,
            locationName = locationName,
            valuations = valuations,
            totalValueUsd = valuations.sumOf { it.currentValueUsd },
            byMetal = byMetal,
            currency = currency,
            usdToEurRate = rate,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LocationDetailUiState.Loading,
    )

    fun selectLocation(locationId: Long) {
        selectedLocationId.value = locationId
    }
}
