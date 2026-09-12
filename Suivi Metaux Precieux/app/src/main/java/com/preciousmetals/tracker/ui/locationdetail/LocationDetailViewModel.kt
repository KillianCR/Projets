package com.preciousmetals.tracker.ui.locationdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.PortfolioRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.data.repository.StorageLocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LocationDetailViewModel(
    private val locationId: Long,
    portfolioRepository: PortfolioRepository,
    storageLocationRepository: StorageLocationRepository,
    userPreferences: UserPreferences,
    priceRepository: PriceRepository,
) : ViewModel() {

    val uiState = combine(
        storageLocationRepository.observeAll(),
        portfolioRepository.observePortfolio(),
        userPreferences.displayCurrency,
        priceRepository.usdToEurRate,
    ) { locations, summary, currency, rate ->
        val locationName = locations.firstOrNull { it.id == locationId }?.name ?: "Lieu de stockage"
        val valuations = summary.valuations
            .filter { it.holding.storageLocationId == locationId }
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
}
