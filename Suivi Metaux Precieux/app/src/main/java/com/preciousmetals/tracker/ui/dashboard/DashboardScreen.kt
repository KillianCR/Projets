package com.preciousmetals.tracker.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.HoldingValuation
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.AllocationBarView
import com.preciousmetals.tracker.ui.components.AllocationSlice
import com.preciousmetals.tracker.ui.components.MetalLogo
import com.preciousmetals.tracker.ui.components.MetalPriceTicker
import com.preciousmetals.tracker.ui.theme.brandColor
import com.preciousmetals.tracker.ui.theme.negativeColor
import com.preciousmetals.tracker.ui.theme.positiveColor
import com.preciousmetals.tracker.util.formatFr
import com.preciousmetals.tracker.util.formatGrams
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.formatPercent
import com.preciousmetals.tracker.util.usdTo
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
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
                onDeleteHolding = { holding ->
                    viewModel.deleteHolding(holding)
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Avoir supprimé",
                            actionLabel = "Annuler",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.restoreHolding(holding)
                        }
                    }
                },
                onMetalClick = onMetalClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

private enum class HoldingSortOption(val label: String) {
    DATE_DESC("Plus récent"),
    VALUE_DESC("Valeur"),
    GAIN_DESC("Plus-value"),
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Loaded,
    onCurrencyChange: (Currency) -> Unit,
    onRefresh: () -> Unit,
    onAddHolding: () -> Unit,
    onEditHolding: (Long) -> Unit,
    onDeleteHolding: (Holding) -> Unit,
    onMetalClick: (Metal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = state.summary
    fun money(usd: Double) = formatMoney(usd.usdTo(state.currency, state.usdToEurRate), state.currency)

    var amountsHidden by rememberSaveable { mutableStateOf(false) }
    fun displayMoney(usd: Double) = if (amountsHidden) "••••••" else money(usd)

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortOption by rememberSaveable { mutableStateOf(HoldingSortOption.DATE_DESC) }

    val filteredValuations = remember(summary.valuations, searchQuery, sortOption) {
        val filtered = if (searchQuery.isBlank()) {
            summary.valuations
        } else {
            summary.valuations.filter { valuation ->
                val holding = valuation.holding
                holding.label.contains(searchQuery, ignoreCase = true) ||
                    holding.metal.displayNameFr.contains(searchQuery, ignoreCase = true) ||
                    holding.objectType.displayNameFr.contains(searchQuery, ignoreCase = true)
            }
        }
        when (sortOption) {
            HoldingSortOption.DATE_DESC -> filtered.sortedByDescending { it.holding.purchaseDate }
            HoldingSortOption.VALUE_DESC -> filtered.sortedByDescending { it.currentValueUsd }
            HoldingSortOption.GAIN_DESC -> filtered.sortedByDescending { it.gainLossPercent ?: Double.NEGATIVE_INFINITY }
        }
    }

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
                amountsHidden = amountsHidden,
                onToggleAmountsHidden = { amountsHidden = !amountsHidden },
            )
        }

        item {
            BalanceHero(
                totalUsd = summary.totalValueUsd,
                money = ::money,
                amountsHidden = amountsHidden,
            )
        }

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
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Plus-value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            gainUsd?.let { displayMoney(it) } ?: "—",
                            style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            gainPercent?.let { formatPercent(it) } ?: "—",
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Répartition par métal", style = MaterialTheme.typography.titleMedium)
                        AllocationBarView(
                            slices = summary.byMetal.map { (metal, value) ->
                                AllocationSlice(metal.displayNameFr, value, metal.brandColor())
                            },
                            modifier = Modifier.padding(top = 14.dp),
                        )
                    }
                }
            }
        }

        item {
            val countLabel = if (filteredValuations.size != summary.valuations.size) {
                "Vos avoirs (${filteredValuations.size} / ${summary.valuations.size})"
            } else {
                "Vos avoirs (${summary.valuations.size})"
            }
            SectionTitle(countLabel)
        }

        if (summary.valuations.size > 1) {
            item {
                SearchAndSortRow(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    sortOption = sortOption,
                    onSortOptionChange = { sortOption = it },
                )
            }
        }

        if (summary.valuations.isEmpty()) {
            item { EmptyHoldingsState(onAddHolding = onAddHolding) }
        } else if (filteredValuations.isEmpty()) {
            item {
                Text(
                    "Aucun avoir ne correspond à \"$searchQuery\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            items(filteredValuations, key = { it.holding.id }) { valuation ->
                HoldingRow(
                    valuation = valuation,
                    money = ::displayMoney,
                    onClick = { onEditHolding(valuation.holding.id) },
                    onDelete = { onDeleteHolding(valuation.holding) },
                )
            }
        }
    }
}

@Composable
private fun SearchAndSortRow(
    query: String,
    onQueryChange: (String) -> Unit,
    sortOption: HoldingSortOption,
    onSortOptionChange: (HoldingSortOption) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    )

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Rechercher un avoir", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Effacer la recherche", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
        )
        Box {
            Surface(
                onClick = { sortMenuExpanded = true },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = "Trier : ${sortOption.label}",
                    )
                }
            }
            DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                HoldingSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSortOptionChange(option)
                            sortMenuExpanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHoldingsState(onAddHolding: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Icon(
                Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
            )
        }
        Text(
            "Aucun avoir pour le moment",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "Ajoutez votre premier lingot, pièce ou bijou pour commencer à suivre sa valeur.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        TextButton(onClick = onAddHolding, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text("  Ajouter un avoir")
        }
    }
}

@Composable
private fun DashboardHeader(
    currency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    onRefresh: () -> Unit,
    amountsHidden: Boolean,
    onToggleAmountsHidden: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onRefresh) {
            Icon(Icons.Outlined.Refresh, contentDescription = "Actualiser les cours")
        }
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text("Bonjour", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Mon portefeuille", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompactCurrencyToggle(currency = currency, onToggle = {
                onCurrencyChange(if (currency == Currency.EUR) Currency.USD else Currency.EUR)
            })
            IconButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleAmountsHidden()
            }) {
                Icon(
                    if (amountsHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (amountsHidden) "Afficher les montants" else "Masquer les montants",
                )
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
        modifier = Modifier.padding(end = 4.dp).defaultMinSize(minWidth = 44.dp, minHeight = 44.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = currency.code,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
    }
}

@Composable
private fun BalanceHero(totalUsd: Double, money: (Double) -> String, amountsHidden: Boolean) {
    // A brief count-up (rather than snapping) whenever the total changes — on first load it
    // animates up from zero, closer to the "odometer" feel neobank balance screens go for.
    val animatedTotal by animateFloatAsState(
        targetValue = totalUsd.toFloat(),
        animationSpec = tween(durationMillis = 700),
        label = "balanceCountUp",
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Valeur totale", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            if (amountsHidden) "••••••" else money(animatedTotal.toDouble()),
            style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
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
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "holdingRowScale")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick),
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
                    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteConfirm = true
                }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer cet avoir ?") },
            text = { Text("\"${holding.label.ifBlank { holding.metal.displayNameFr }}\" sera supprimé. Vous pourrez l'annuler juste après.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
            },
        )
    }
}
