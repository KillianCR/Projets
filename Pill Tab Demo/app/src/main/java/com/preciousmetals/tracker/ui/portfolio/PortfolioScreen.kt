package com.preciousmetals.tracker.ui.portfolio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.components.AllocationBarView
import com.preciousmetals.tracker.ui.components.AllocationSlice
import com.preciousmetals.tracker.ui.components.CombinedMetalsLogo
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.HoldingRow
import com.preciousmetals.tracker.ui.components.MetalLogo
import com.preciousmetals.tracker.ui.components.MetalPriceTicker
import com.preciousmetals.tracker.ui.components.PercentPill
import com.preciousmetals.tracker.ui.components.topFadingEdge
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.navigation.bottomNavOverlayPadding
import com.preciousmetals.tracker.ui.theme.ChipBorderDark
import com.preciousmetals.tracker.ui.theme.ChipSurfaceDark
import com.preciousmetals.tracker.ui.theme.EurPillBorderDark
import com.preciousmetals.tracker.ui.theme.EurPillSurfaceDark
import com.preciousmetals.tracker.ui.theme.HeaderIconMutedDark
import com.preciousmetals.tracker.ui.theme.NearBlackEmber
import com.preciousmetals.tracker.ui.theme.OnBackgroundDark
import com.preciousmetals.tracker.ui.theme.TextMuted38Dark
import com.preciousmetals.tracker.ui.theme.TextMuted56Dark
import com.preciousmetals.tracker.ui.theme.TextMuted80Dark
import com.preciousmetals.tracker.ui.theme.brandColor
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * "Mon portefeuille" imported from Suivi Métaux, wired to static sample data instead of the real
 * ViewModel/repositories — a design-only sandbox, so tweaking colors/spacing here never touches
 * (or needs to rebuild) the real app.
 */
private enum class HoldingSortOption(val label: String) {
    DATE_DESC("Plus récent"),
    VALUE_DESC("Valeur"),
    GAIN_DESC("Plus-value"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(modifier: Modifier = Modifier) {
    val summary = remember { samplePortfolioSummary() }
    val livePricesUsdPerGram = remember { sampleLivePricesUsdPerGram() }
    val sparklineByMetal = remember { sampleSparklineByMetal() }
    val usdToEurRate = 0.92

    var currency by rememberSaveable { mutableStateOf(Currency.EUR) }
    var amountsHidden by rememberSaveable { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortOption by rememberSaveable { mutableStateOf(HoldingSortOption.DATE_DESC) }
    var metalFilter by rememberSaveable { mutableStateOf<Metal?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun money(usd: Double) = formatMoney(usd.usdTo(currency, usdToEurRate), currency)
    fun displayMoney(usd: Double) = if (amountsHidden) "••••••" else money(usd)

    val filteredValuations = remember(summary.valuations, searchQuery, sortOption, metalFilter) {
        val byMetal = if (metalFilter == null) {
            summary.valuations
        } else {
            summary.valuations.filter { it.holding.metal == metalFilter }
        }
        val filtered = if (searchQuery.isBlank()) {
            byMetal
        } else {
            byMetal.filter { valuation ->
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

    Box(modifier = modifier) {
        Scaffold(containerColor = Color.Transparent) { padding ->
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(padding).fillMaxSize().topFadingEdge(listState),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = bottomNavContentPadding()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    DashboardHeader(
                        currency = currency,
                        onCurrencyChange = { currency = it },
                        onRefresh = {
                            if (!isRefreshing) {
                                isRefreshing = true
                                scope.launch {
                                    delay(900)
                                    isRefreshing = false
                                    snackbarHostState.showSnackbar("Cours actualisés", duration = SnackbarDuration.Short)
                                }
                            }
                        },
                        isRefreshing = isRefreshing,
                        amountsHidden = amountsHidden,
                        onToggleAmountsHidden = { amountsHidden = !amountsHidden },
                    )
                }

                item {
                    BalanceHero(totalUsd = summary.totalValueUsd, money = ::money, amountsHidden = amountsHidden)
                }

                item {
                    QuickActionsRow(
                        onAddHolding = { scope.launch { snackbarHostState.showSnackbar("Démo — non implémenté") } },
                        onViewStorageLocations = { scope.launch { snackbarHostState.showSnackbar("Démo — non implémenté") } },
                    )
                }

                item {
                    val costBasisUsd = summary.totalCostBasisUsd
                    val gainUsd = summary.totalGainLossUsd
                    val gainPercent = summary.totalGainLossPercent
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Coût total achat", style = MaterialTheme.typography.labelMedium, color = TextMuted38Dark)
                                Text(
                                    costBasisUsd?.let { displayMoney(it) } ?: "—",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp, fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Column {
                                Text("Plus-value", style = MaterialTheme.typography.labelMedium, color = TextMuted38Dark)
                                Text(
                                    gainUsd?.let { displayMoney(it) } ?: "—",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp, fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (gainPercent != null) {
                                PercentPill(percent = gainPercent)
                            } else {
                                Text("—", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            "COURS DES MÉTAUX",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, letterSpacing = 0.4.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted56Dark,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                        MetalPriceTicker(
                            metals = Metal.entries,
                            pricesUsdPerGram = livePricesUsdPerGram,
                            sparklineByMetal = sparklineByMetal,
                            currency = currency,
                            usdToEurRate = usdToEurRate,
                            onMetalClick = {},
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                if (summary.byMetal.isNotEmpty()) {
                    item {
                        var expanded by rememberSaveable { mutableStateOf(false) }
                        GlassCard(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Répartition par métal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                    Icon(
                                        if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = if (expanded) "Réduire" else "Développer",
                                        tint = TextMuted38Dark,
                                    )
                                }
                                if (expanded) {
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
                }

                item {
                    val countLabel = if (filteredValuations.size != summary.valuations.size) {
                        "Vos avoirs (${filteredValuations.size} / ${summary.valuations.size})"
                    } else {
                        "Vos avoirs (${summary.valuations.size})"
                    }
                    Text(countLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                if (summary.valuations.size > 1) {
                    item {
                        SearchAndSortRow(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            sortOption = sortOption,
                            onSortOptionChange = { sortOption = it },
                            currency = currency,
                            metalFilter = metalFilter,
                            onMetalFilterChange = { metalFilter = it },
                        )
                    }
                }

                if (filteredValuations.isEmpty()) {
                    item {
                        val message = if (searchQuery.isNotBlank()) {
                            "Aucun avoir ne correspond à \"$searchQuery\"."
                        } else {
                            "Aucun avoir en ${metalFilter?.displayNameFr}."
                        }
                        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    items(filteredValuations, key = { it.holding.id }) { valuation ->
                        HoldingRow(
                            valuation = valuation,
                            currency = currency,
                            money = ::displayMoney,
                            onClick = { scope.launch { snackbarHostState.showSnackbar("Démo — non implémenté") } },
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomNavOverlayPadding()),
        ) { data ->
            Snackbar(snackbarData = data, containerColor = NearBlackEmber, contentColor = OnBackgroundDark, actionColor = OnBackgroundDark)
        }
    }
}

@Composable
private fun SearchAndSortRow(
    query: String,
    onQueryChange: (String) -> Unit,
    sortOption: HoldingSortOption,
    onSortOptionChange: (HoldingSortOption) -> Unit,
    currency: Currency,
    metalFilter: Metal?,
    onMetalFilterChange: (Metal?) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var logoMenuExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = Color.Transparent,
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
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.FilterList, contentDescription = "Trier : ${sortOption.label}")
                }
            }
            DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                HoldingSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { onSortOptionChange(option); sortMenuExpanded = false },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    )
                }
            }
        }
        Box {
            Surface(
                onClick = { logoMenuExpanded = true },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (metalFilter == null) {
                        CombinedMetalsLogo(size = 26.dp)
                    } else {
                        MetalLogo(metal = metalFilter, currency = currency, size = 26.dp)
                    }
                }
            }
            DropdownMenu(expanded = logoMenuExpanded, onDismissRequest = { logoMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Tous les métaux") },
                    trailingIcon = { if (metalFilter == null) Icon(Icons.Outlined.Check, contentDescription = null) },
                    onClick = { onMetalFilterChange(null); logoMenuExpanded = false },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                )
                Metal.entries.forEach { metal ->
                    DropdownMenuItem(
                        text = { Text(metal.displayNameFr) },
                        trailingIcon = { if (metalFilter == metal) Icon(Icons.Outlined.Check, contentDescription = null) },
                        onClick = { onMetalFilterChange(metal); logoMenuExpanded = false },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    currency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    amountsHidden: Boolean,
    onToggleAmountsHidden: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Mon portefeuille", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            IconButton(
                onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onRefresh() },
                enabled = !isRefreshing,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = HeaderIconMutedDark)
                } else {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Actualiser les cours", tint = HeaderIconMutedDark)
                }
            }
            CompactCurrencyToggle(
                currency = currency,
                onToggle = { onCurrencyChange(if (currency == Currency.EUR) Currency.USD else Currency.EUR) },
                modifier = Modifier.align(Alignment.Center),
            )
            IconButton(
                onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onToggleAmountsHidden() },
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(
                    if (amountsHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (amountsHidden) "Afficher les montants" else "Masquer les montants",
                    tint = HeaderIconMutedDark,
                )
            }
        }
    }
}

@Composable
private fun CompactCurrencyToggle(currency: Currency, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = EurPillSurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, EurPillBorderDark),
        modifier = modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = currency.code,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
                color = TextMuted80Dark,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun BalanceHero(totalUsd: Double, money: (Double) -> String, amountsHidden: Boolean) {
    val animatedTotal by animateFloatAsState(
        targetValue = totalUsd.toFloat(),
        animationSpec = tween(durationMillis = 700),
        label = "balanceCountUp",
    )
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "VALEUR TOTALE",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 1.sp),
            fontWeight = FontWeight.Normal,
            color = TextMuted38Dark,
        )
        Text(
            if (amountsHidden) "••••••" else money(animatedTotal.toDouble()),
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 46.sp, letterSpacing = (-1.2).sp, fontFeatureSettings = "tnum"),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun QuickActionsRow(onAddHolding: () -> Unit, onViewStorageLocations: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionButton(icon = Icons.Outlined.Add, label = "Ajouter", onClick = onAddHolding, isPrimary = true, modifier = Modifier.weight(1f))
        QuickActionButton(icon = Icons.Outlined.Inventory2, label = "Stockage", onClick = onViewStorageLocations, isPrimary = false, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isPrimary) MaterialTheme.colorScheme.primary else ChipSurfaceDark
    val contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else Color.White
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = if (isPrimary) null else androidx.compose.foundation.BorderStroke(1.dp, ChipBorderDark),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = label, tint = contentColor)
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}
