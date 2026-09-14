package com.preciousmetals.tracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.AreaChartView
import com.preciousmetals.tracker.ui.components.ChartPoint
import com.preciousmetals.tracker.ui.components.CompactTopBar
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.GlassChip
import com.preciousmetals.tracker.ui.components.MarketStatsCard
import com.preciousmetals.tracker.ui.components.topFadingEdge
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.theme.TextMuted33Dark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo

private val portfolioHistoryRanges = listOf(7 to "7j", 30 to "30j", 90 to "90j", 365 to "1an", 1825 to "5ans")

/**
 * The portfolio's total value over time — same visual language as [PriceHistoryScreen] (range
 * chips, headline + area chart in a [GlassCard], [MarketStatsCard]), but one line for the whole
 * portfolio instead of a per-metal spot price. A holding only contributes to a given day once it's
 * actually been purchased (see [com.preciousmetals.tracker.data.repository.PortfolioRepository.observeValueHistory]),
 * so the curve never shows value for a quantity of metal the user didn't own yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioHistoryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val viewModel: PortfolioHistoryViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                PortfolioHistoryViewModel(
                    portfolioRepository = container.portfolioRepository,
                    userPreferences = container.userPreferences,
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { CompactTopBar(title = "Historique du portefeuille", onBack = onBack) },
        // CompactTopBar and bottomNavContentPadding() below already own the top/bottom system-bar
        // insets; leaving Scaffold's own default would double them up under edge-to-edge.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .topFadingEdge(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = bottomNavContentPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                portfolioHistoryRanges.forEach { (days, label) ->
                    val selected = (state as? PortfolioHistoryUiState.Loaded)?.rangeDays == days
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
                is PortfolioHistoryUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is PortfolioHistoryUiState.Loaded -> {
                    val color = MaterialTheme.colorScheme.primary

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val totalUsd = current.currentValueUsd
                            if (totalUsd != null) {
                                Text(
                                    formatMoney(totalUsd.usdTo(current.currency, current.usdToEurRate), current.currency),
                                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 26.sp, letterSpacing = (-0.5).sp),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "Valeur totale actuelle",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = TextMuted44Dark,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                                )
                            }

                            AreaChartView(
                                points = current.history.map {
                                    ChartPoint(
                                        date = it.date,
                                        value = it.totalValueUsd.usdTo(current.currency, current.usdToEurRate),
                                        // Gain/loss per day is measured against what was actually
                                        // paid for the holdings owned that day, not the chart's own
                                        // first day — otherwise adding a holding mid-range would
                                        // read as an instant price gain equal to its whole value.
                                        referenceValue = it.totalCostBasisUsd.usdTo(current.currency, current.usdToEurRate),
                                    )
                                },
                                lineColor = color,
                                valueFormatter = { formatMoney(it, current.currency) },
                                modifier = Modifier.padding(top = 12.dp),
                                showGainAmount = true,
                            )
                        }
                    }

                    Text(
                        "Touchez ou glissez sur le graphique pour inspecter un point. Chaque avoir ne " +
                            "compte dans la courbe qu'à partir de sa date d'achat.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 19.sp),
                        color = TextMuted33Dark,
                    )

                    MarketStatsCard(stats = current.marketStats, title = "PERFORMANCE DU PORTEFEUILLE")
                }
            }
        }
    }
}
