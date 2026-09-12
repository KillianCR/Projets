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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.AreaChartView
import com.preciousmetals.tracker.ui.components.ChartPoint
import com.preciousmetals.tracker.ui.components.CompactTopBar
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.GlassChip
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.ui.components.PercentPill
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.theme.IconTileSurfaceDark
import com.preciousmetals.tracker.ui.theme.TextMuted33Dark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.ui.theme.TextMuted67Dark
import com.preciousmetals.tracker.ui.theme.brandColor
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo

private val ranges = listOf(7 to "7j", 30 to "30j", 90 to "90j", 365 to "1an", 1825 to "5ans")

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
        topBar = { CompactTopBar(title = "Historique des cours") },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = bottomNavContentPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Metal.entries, key = { it.name }) { metal ->
                    val selected = (state as? PriceHistoryUiState.Loaded)?.metal == metal
                    GlassChip(
                        selected = selected,
                        onClick = { viewModel.selectMetal(metal) },
                        label = metal.displayNameFr,
                        leadingContent = { MetalBadge(metal = metal) },
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ranges.forEach { (days, label) ->
                    val selected = (state as? PriceHistoryUiState.Loaded)?.rangeDays == days
                    GlassChip(
                        selected = selected,
                        onClick = { viewModel.selectRange(days) },
                        label = label,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 9.dp),
                        modifier = Modifier.weight(1f),
                    )
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
                                val perBigUnit = perGramCurrent * current.metal.bigUnitGrams
                                Text(
                                    buildAnnotatedString {
                                        append(formatMoney(perBigUnit.usdTo(current.currency, current.usdToEurRate), current.currency))
                                        withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Normal, color = TextMuted44Dark, fontSize = 14.sp)) {
                                            append(" / ${current.metal.bigUnitLabel}")
                                        }
                                    },
                                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 26.sp, letterSpacing = (-0.5).sp),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    formatMoney((perGramCurrent * current.metal.smallUnitGrams).usdTo(current.currency, current.usdToEurRate), current.currency) +
                                        " / ${current.metal.smallUnitLabel}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = TextMuted44Dark,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                                )
                            }

                            AreaChartView(
                                points = current.history.map {
                                    ChartPoint(
                                        it.date,
                                        (it.priceUsdPerGram * current.metal.smallUnitGrams).usdTo(current.currency, current.usdToEurRate),
                                    )
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 19.sp),
                        color = TextMuted33Dark,
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
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = TextMuted44Dark,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
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
                shape = RoundedCornerShape(10.dp),
                color = IconTileSurfaceDark,
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = TextMuted67Dark,
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
