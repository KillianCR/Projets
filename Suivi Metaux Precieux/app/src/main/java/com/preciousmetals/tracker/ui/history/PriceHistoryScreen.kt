package com.preciousmetals.tracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.GRAMS_PER_TROY_OUNCE
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.LineChartView
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo

private val ranges = listOf(7 to "7j", 30 to "30j", 90 to "90j", 365 to "1an")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceHistoryScreen(modifier: Modifier = Modifier, initialMetal: Metal? = null) {
    val container = LocalAppContainer.current
    val viewModel: PriceHistoryViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                PriceHistoryViewModel(
                    priceRepository = container.priceRepository,
                    userPreferences = container.userPreferences,
                    initialMetal = initialMetal ?: Metal.GOLD,
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Historique des cours") }) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metal.entries.forEach { metal ->
                    val selected = (state as? PriceHistoryUiState.Loaded)?.metal == metal
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectMetal(metal) },
                        label = { Text(metal.displayNameFr) },
                        leadingIcon = { MetalBadge(metal = metal) },
                    )
                }
            }

            Row(modifier = Modifier.padding(top = 12.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ranges.forEachIndexed { index, (days, label) ->
                        val selected = (state as? PriceHistoryUiState.Loaded)?.rangeDays == days
                        SegmentedButton(
                            selected = selected,
                            onClick = { viewModel.selectRange(days) },
                            shape = SegmentedButtonDefaults.itemShape(index, ranges.size),
                        ) { Text(label) }
                    }
                }
            }

            when (val current = state) {
                is PriceHistoryUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is PriceHistoryUiState.Loaded -> {
                    val perGramCurrent = current.latestPriceUsdPerGram
                    if (perGramCurrent != null) {
                        val perOunce = perGramCurrent * GRAMS_PER_TROY_OUNCE
                        Text(
                            text = "Once : " + formatMoney(perOunce.usdTo(current.currency, current.usdToEurRate), current.currency) +
                                "   ·   Gramme : " + formatMoney(perGramCurrent.usdTo(current.currency, current.usdToEurRate), current.currency),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }

                    LineChartView(
                        values = current.history.map { it.priceUsdPerGram.usdTo(current.currency, current.usdToEurRate) },
                        lineColor = Color(current.metal.colorHex),
                        modifier = Modifier.padding(top = 16.dp),
                    )

                    Text(
                        "Les cours sont enregistrés localement à chaque actualisation : plus vous " +
                            "utilisez l'application, plus l'historique s'étoffe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}
