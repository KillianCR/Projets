package com.preciousmetals.tracker.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.preciousmetals.tracker.ui.components.MetalLogo
import com.preciousmetals.tracker.ui.components.MetalPriceTicker
import com.preciousmetals.tracker.ui.components.PieChartView
import com.preciousmetals.tracker.ui.components.PieSlice
import com.preciousmetals.tracker.ui.theme.brandColor
import com.preciousmetals.tracker.ui.theme.negativeColor
import com.preciousmetals.tracker.ui.theme.positiveColor
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

    Scaffold(modifier = modifier) { padding ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is DashboardUiState.Loaded -> DashboardContent(
                state = state,
                onCurrencyChange = viewModel::setCurrency,
                onRefresh = viewModel::refresh,
                onAddHolding = onAddHolding,
                onEditHolding = onEditHolding,
                onDeleteHolding = viewModel::deleteHolding,
                onMetalClick = onMetalClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Loaded,
    onCurrencyChange: (Currency) -> Unit,
    onRefresh: () -> Unit,
    onAddHolding: () -> Unit,
    onEditHolding: (Long) -> Unit,
    onDeleteHolding: (com.preciousmetals.tracker.domain.model.Holding) -> Unit,
    onMetalClick: (Metal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = state.summary
    fun money(usd: Double) = formatMoney(usd.usdTo(state.currency, state.usdToEurRate), state.currency)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            DashboardHeader(
                currency = state.currency,
                onCurrencyChange = onCurrencyChange,
                onRefresh = onRefresh,
            )
        }

        item { BalanceHero(totalUsd = summary.totalValueUsd, money = ::money) }

        item {
            QuickActionsRow(
                onAddHolding = onAddHolding,
                onMetalClick = { onMetalClick(Metal.GOLD) },
            )
        }

        if (state.refreshError != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
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
            val gainUsd = summary.totalGainLossUsd
            val gainPercent = summary.totalGainLossPercent
            val color = when {
                gainUsd == null -> MaterialTheme.colorScheme.onSurfaceVariant
                gainUsd >= 0 -> positiveColor()
                else -> negativeColor()
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Plus-value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(gainUsd?.let { money(it) } ?: "—", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            gainPercent?.let { formatPercent(it) } ?: "—",
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }

        item {
            Column {
                SectionTitle("Vos métaux")
                MetalPriceTicker(
                    metals = Metal.entries,
                    pricesUsdPerGram = state.livePricesUsdPerGram,
                    sparklineByMetal = state.sparklineByMetal,
                    currency = state.currency,
                    usdToEurRate = state.usdToEurRate,
                    onMetalClick = onMetalClick,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        if (summary.byMetal.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Répartition par métal", style = MaterialTheme.typography.titleMedium)
                        PieChartView(
                            slices = summary.byMetal.map { (metal, value) ->
                                PieSlice(metal.displayNameFr, value, metal.brandColor())
                            }
                        )
                    }
                }
            }
        }

        item {
            SectionTitle("Vos avoirs (${summary.valuations.size})")
        }

        if (summary.valuations.isEmpty()) {
            item {
                Text(
                    "Aucun avoir pour le moment. Appuyez sur \"Ajouter\" pour ajouter votre premier " +
                        "lingot, pièce ou bijou.",
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
private fun DashboardHeader(
    currency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Bonjour", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Mon portefeuille", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompactCurrencyToggle(currency = currency, onToggle = {
                onCurrencyChange(if (currency == Currency.EUR) Currency.USD else Currency.EUR)
            })
            IconButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Actualiser les cours")
            }
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
private fun BalanceHero(totalUsd: Double, money: (Double) -> String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Valeur totale", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            money(totalUsd),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun QuickActionsRow(onAddHolding: () -> Unit, onMetalClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionButton(
            icon = Icons.Outlined.Add,
            label = "Ajouter",
            onClick = onAddHolding,
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            icon = Icons.AutoMirrored.Outlined.ShowChart,
            label = "Cours",
            onClick = onMetalClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun HoldingRow(
    valuation: HoldingValuation,
    money: (Double) -> String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val holding = valuation.holding
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetalLogo(metal = holding.metal, size = 40.dp)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(holding.label.ifBlank { holding.metal.displayNameFr }, fontWeight = FontWeight.SemiBold)
                Text(
                    "${holding.objectType.displayNameFr} · ${formatGrams(holding.grams)} · ${holding.purchaseDate.formatFr()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val percent = valuation.gainLossPercent
                if (percent != null) {
                    Text(
                        formatPercent(percent),
                        color = if (percent >= 0) positiveColor() else negativeColor(),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (valuation.hasLivePrice) money(valuation.currentValueUsd) else "…",
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
