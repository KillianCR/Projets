package com.preciousmetals.tracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.HoldingValuation
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.ui.components.MetalPriceTicker
import com.preciousmetals.tracker.ui.components.PieChartView
import com.preciousmetals.tracker.ui.components.PieSlice
import com.preciousmetals.tracker.ui.components.StatCard
import com.preciousmetals.tracker.ui.theme.NegativeRed
import com.preciousmetals.tracker.ui.theme.PositiveGreen
import com.preciousmetals.tracker.util.formatFr
import com.preciousmetals.tracker.util.formatGrams
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.formatPercent
import com.preciousmetals.tracker.util.usdTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddHolding: () -> Unit,
    onEditHolding: (Long) -> Unit,
    onMetalClick: (Metal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalAppContainer.current
    val viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DashboardViewModel(
                    portfolioRepository = container.portfolioRepository,
                    priceRepository = container.priceRepository,
                    userPreferences = container.userPreferences,
                    holdingRepository = container.holdingRepository,
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mon portefeuille") },
                actions = {
                    val loaded = uiState as? DashboardUiState.Loaded
                    if (loaded != null) {
                        CompactCurrencyToggle(
                            currency = loaded.currency,
                            onToggle = {
                                viewModel.setCurrency(if (loaded.currency == Currency.EUR) Currency.USD else Currency.EUR)
                            },
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualiser les cours")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHolding) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un avoir")
            }
        },
    ) { padding ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is DashboardUiState.Loaded -> DashboardContent(
                state = state,
                onEditHolding = onEditHolding,
                onDeleteHolding = viewModel::deleteHolding,
                onMetalClick = onMetalClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun CompactCurrencyToggle(currency: Currency, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(end = 4.dp),
    ) {
        Text(
            text = currency.code,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Loaded,
    onEditHolding: (Long) -> Unit,
    onDeleteHolding: (com.preciousmetals.tracker.domain.model.Holding) -> Unit,
    onMetalClick: (Metal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = state.summary
    fun money(usd: Double) = formatMoney(usd.usdTo(state.currency, state.usdToEurRate), state.currency)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MetalPriceTicker(
                metals = Metal.entries,
                pricesUsdPerGram = state.livePricesUsdPerGram,
                currency = state.currency,
                usdToEurRate = state.usdToEurRate,
                onMetalClick = onMetalClick,
            )
        }

        if (state.refreshError != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = state.refreshError,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        item {
            StatCard(title = "Valeur totale", value = money(summary.totalValueUsd))
        }

        item {
            val gainUsd = summary.totalGainLossUsd
            val gainPercent = summary.totalGainLossPercent
            val color = when {
                gainUsd == null -> null
                gainUsd >= 0 -> PositiveGreen
                else -> NegativeRed
            }
            StatCard(
                title = "Plus-value",
                value = gainUsd?.let { money(it) } ?: "—",
                subtitle = gainPercent?.let { formatPercent(it) }
                    ?: "Saisissez un prix d'achat ou patientez le temps que l'historique se construise",
                subtitleColor = color,
            )
        }

        if (summary.byMetal.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Répartition par métal", style = MaterialTheme.typography.titleMedium)
                        PieChartView(
                            slices = summary.byMetal.map { (metal, value) ->
                                PieSlice(metal.displayNameFr, value, Color(metal.colorHex))
                            }
                        )
                    }
                }
            }
        }

        item {
            Text("Vos avoirs (${summary.valuations.size})", style = MaterialTheme.typography.titleMedium)
        }

        if (summary.valuations.isEmpty()) {
            item {
                Text(
                    "Aucun avoir pour le moment. Appuyez sur + pour ajouter votre premier lingot, " +
                        "pièce ou bijou.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(summary.valuations, key = { it.holding.id }) { valuation ->
                HoldingRow(
                    valuation = valuation,
                    money = ::money,
                    onClick = { onEditHolding(valuation.holding.id) },
                    onDelete = { onDeleteHolding(valuation.holding) },
                )
            }
        }
    }
}

@Composable
private fun HoldingRow(
    valuation: HoldingValuation,
    money: (Double) -> String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val holding = valuation.holding
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetalBadge(metal = holding.metal)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(holding.label.ifBlank { holding.metal.displayNameFr }, fontWeight = FontWeight.SemiBold)
                Text(
                    "${holding.objectType.displayNameFr} · ${formatGrams(holding.grams)} · achat le ${holding.purchaseDate.formatFr()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val percent = valuation.gainLossPercent
                if (percent != null) {
                    Text(
                        formatPercent(percent),
                        color = if (percent >= 0) PositiveGreen else NegativeRed,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (valuation.hasLivePrice) money(valuation.currentValueUsd) else "…",
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
