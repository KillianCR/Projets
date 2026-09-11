package com.preciousmetals.tracker.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.GoalRepository
import com.preciousmetals.tracker.data.repository.HoldingRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.data.repository.StorageLocationRepository
import com.preciousmetals.tracker.domain.model.Goal
import com.preciousmetals.tracker.domain.model.GoalTargetType
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.StorageLocation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToolsViewModel(
    private val goalRepository: GoalRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val holdingRepository: HoldingRepository,
    private val priceRepository: PriceRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val uiState = combine(
        goalRepository.observeAll(),
        storageLocationRepository.observeAll(),
        holdingRepository.observeAll(),
        priceRepository.observeAllLatestPricesUsdPerGram(),
        userPreferences.displayCurrency,
        priceRepository.usdToEurRate,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val goals = values[0] as List<Goal>
        @Suppress("UNCHECKED_CAST")
        val locations = values[1] as List<StorageLocation>
        @Suppress("UNCHECKED_CAST")
        val holdings = values[2] as List<Holding>
        @Suppress("UNCHECKED_CAST")
        val livePrices = values[3] as Map<Metal, Double?>
        val currency = values[4] as com.preciousmetals.tracker.domain.model.Currency
        val rate = values[5] as Double

        val gramsByMetal = holdings.groupBy { it.metal }.mapValues { (_, list) -> list.sumOf { it.grams } }
        val totalValueUsd = holdings.sumOf { holding -> (livePrices[holding.metal] ?: 0.0) * holding.grams }

        val goalProgress = goals.map { goal ->
            val current = when (goal.targetType) {
                GoalTargetType.WEIGHT_GRAMS -> goal.metal?.let { gramsByMetal[it] } ?: 0.0
                GoalTargetType.VALUE_USD -> totalValueUsd
            }
            val fraction = if (goal.targetAmount > 0.0) (current / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
            GoalProgress(goal = goal, currentAmount = current, progressFraction = fraction)
        }

        val countByLocation = holdings
            .mapNotNull { it.storageLocationId }
            .groupingBy { it }
            .eachCount()

        ToolsUiState(
            goals = goalProgress,
            storageLocations = locations,
            holdingCountByLocationId = countByLocation,
            currency = currency,
            usdToEurRate = rate,
            livePricesUsdPerGram = livePrices,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ToolsUiState())

    fun addGoal(label: String, metal: Metal?, targetType: GoalTargetType, targetAmount: Double) {
        viewModelScope.launch {
            goalRepository.add(
                Goal(
                    label = label,
                    metal = metal,
                    targetType = targetType,
                    targetAmount = targetAmount,
                    createdAtEpochMillis = System.currentTimeMillis(),
                )
            )
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch { goalRepository.delete(goal) }
    }

    fun addStorageLocation(location: StorageLocation) {
        viewModelScope.launch { storageLocationRepository.upsert(location) }
    }

    fun deleteStorageLocation(location: StorageLocation) {
        viewModelScope.launch { storageLocationRepository.delete(location) }
    }
}
