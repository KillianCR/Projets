package com.preciousmetals.tracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.GRAMS_PER_TROY_OUNCE
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.AreaChartView
import com.preciousmetals.tracker.ui.components.ChartPoint
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.ui.components.PercentPill
import com.preciousmetals.tracker.ui.theme.brandColor
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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Historique des cours") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Metal.entries, key = { it.name }) { metal ->
                    val selected = (state as? PriceHistoryUiState.Loaded)?.metal == metal
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectMetal(metal) },
                        label = { Text(metal.displayNameFr) },
                        leadingIcon = { MetalBadge(metal = metal) },
                    )
                }
            }

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

            when (val current = state) {
                is PriceHistoryUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is PriceHistoryUiState.Loaded -> {
                    val color = current.metal.brandColor()

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val perGramCurrent = current.latestPriceUsdPerGram
                            if (perGramCurrent != null) {
                                val perOunce = perGramCurrent * GRAMS_PER_TROY_OUNCE
                                Text(
                                    formatMoney(perOunce.usdTo(current.currency, current.usdToEurRate), current.currency) + " / once",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    formatMoney(perGramCurrent.usdTo(current.currency, current.usdToEurRate), current.currency) + " / gramme",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            AreaChartView(
                                points = current.history.map {
                                    ChartPoint(it.date, it.priceUsdPerGram.usdTo(current.currency, current.usdToEurRate))
                                },
                                lineColor = color,
                                valueFormatter = { formatMoney(it, current.currency) },
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }

                    Text(
                        "Touchez ou glissez sur le graphique pour inspecter un point. L'historique " +
                            "s'étoffe à chaque actualisation des cours.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    MarketStatsCard(stats = current.marketStats)
                }
            }
        }
    }
}

@Composable
private fun MarketStatsCard(stats: List<MarketStat>) {
    Column {
        Text(
            "STATISTIQUES DU MARCHÉ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                stats.forEachIndexed { index, stat ->
                    MarketStatRow(stat = stat)
                    if (index != stats.lastIndex) {
                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketStatRow(stat: MarketStat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                "Performance ${stat.label}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        val percent = stat.percentChange
        if (percent == null) {
            Text("—", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            PercentPill(percent = percent)
        }
    }
}
