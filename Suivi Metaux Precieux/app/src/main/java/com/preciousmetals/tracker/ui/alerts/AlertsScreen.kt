package com.preciousmetals.tracker.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.AlertDirection
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PriceAlert
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.CompactTopBar
import com.preciousmetals.tracker.ui.components.GlassAlertDialog
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.GlassChip
import com.preciousmetals.tracker.ui.components.GlassSegmentedRow
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.ui.components.glassInputFieldColors
import com.preciousmetals.tracker.ui.components.horizontalFadingEdges
import com.preciousmetals.tracker.ui.components.topFadingEdge
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.navigation.bottomNavOverlayPadding
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val viewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AlertsViewModel(
                    alertRepository = container.alertRepository,
                    priceRepository = container.priceRepository,
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { CompactTopBar(title = "Alertes de cours") },
        // CompactTopBar and bottomNavContentPadding()/bottomNavOverlayPadding() below already own
        // the top/bottom system-bar insets; leaving Scaffold's own default would double them up
        // under edge-to-edge.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = bottomNavOverlayPadding()),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une alerte")
            }
        },
    ) { padding ->
        if (state.alerts.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Aucune alerte. Créez-en une pour être notifié quand un cours atteint un seuil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted44Dark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            }
        } else {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(padding).fillMaxSize().topFadingEdge(listState),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomNavContentPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.alerts, key = { it.id }) { alert ->
                    AlertRow(
                        alert = alert,
                        currencyLabel = formatMoney(
                            (alert.thresholdUsdPerGram * alert.metal.smallUnitGrams).usdTo(alert.currency, state.usdToEurRate),
                            alert.currency,
                        ) + "/" + (if (alert.metal.smallUnitLabel == "kilo") "kg" else "g"),
                        onToggle = { viewModel.toggleAlert(alert) },
                        onDelete = { viewModel.deleteAlert(alert) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAlertDialog(
            livePricesUsdPerGram = state.livePricesUsdPerGram,
            usdToEurRate = state.usdToEurRate,
            onDismiss = { showAddDialog = false },
            onConfirm = { metal, direction, percent, currency ->
                viewModel.addAlertByPercent(metal, direction, percent, currency)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun AlertRow(alert: PriceAlert, currencyLabel: String, onToggle: () -> Unit, onDelete: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MetalBadge(metal = alert.metal)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                val directionLabel = if (alert.direction == AlertDirection.ABOVE) "dépasse" else "descend sous"
                Text(
                    "${alert.metal.displayNameFr} $directionLabel $currencyLabel",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (alert.lastTriggeredAtEpochMillis != null) {
                    Text(
                        "Déclenchée",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted44Dark,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Switch(checked = alert.enabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private val AlertPercentPresets = listOf(5, 10, 15, 20, 30, 40, 50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlertDialog(
    livePricesUsdPerGram: Map<Metal, Double?>,
    usdToEurRate: Double,
    onDismiss: () -> Unit,
    onConfirm: (Metal, AlertDirection, Double, Currency) -> Unit,
) {
    var metal by remember { mutableStateOf(Metal.GOLD) }
    var direction by remember { mutableStateOf(AlertDirection.ABOVE) }
    var currency by remember { mutableStateOf(Currency.EUR) }
    var percentText by remember { mutableStateOf(AlertPercentPresets.first().toString()) }

    val percent = percentText.replace(',', '.').toDoubleOrNull()
    val currentPriceUsdPerGram = livePricesUsdPerGram[metal]
    val sign = if (direction == AlertDirection.ABOVE) 1.0 else -1.0

    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle alerte") },
        text = {
            Column {
                val metalListState = rememberLazyListState()
                LazyRow(
                    state = metalListState,
                    modifier = Modifier.horizontalFadingEdges(metalListState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(Metal.entries, key = { it.name }) { m ->
                        GlassChip(
                            selected = metal == m,
                            onClick = { metal = m },
                            label = m.displayNameFr,
                            leadingContent = { MetalBadge(metal = m) },
                        )
                    }
                }
                GlassSegmentedRow(
                    options = listOf(AlertDirection.ABOVE to "Au-dessus", AlertDirection.BELOW to "En dessous"),
                    selected = direction,
                    onSelect = { direction = it },
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    "Devise de l'alerte",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted44Dark,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                GlassSegmentedRow(
                    options = listOf(Currency.EUR to "EUR", Currency.USD to "USD"),
                    selected = currency,
                    onSelect = { currency = it },
                )
                Text(
                    "Seuil : variation par rapport au cours actuel",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted44Dark,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                val percentListState = rememberLazyListState()
                LazyRow(
                    state = percentListState,
                    modifier = Modifier.horizontalFadingEdges(percentListState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(AlertPercentPresets) { preset ->
                        GlassChip(
                            selected = percent == preset.toDouble(),
                            onClick = { percentText = preset.toString() },
                            label = "$preset %",
                        )
                    }
                }
                OutlinedTextField(
                    value = percentText,
                    onValueChange = { percentText = it },
                    label = { Text("Pourcentage personnalisé (%)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true,
                    colors = glassInputFieldColors(),
                )
                val previewText = when {
                    currentPriceUsdPerGram == null -> "Cours pas encore disponible pour ${metal.displayNameFr}."
                    percent == null || percent <= 0.0 -> null
                    else -> {
                        val targetUsdPerGram = currentPriceUsdPerGram * (1.0 + sign * percent / 100.0)
                        val targetLabel = formatMoney(
                            (targetUsdPerGram * metal.smallUnitGrams).usdTo(currency, usdToEurRate),
                            currency,
                        ) + "/" + (if (metal.smallUnitLabel == "kilo") "kg" else "g")
                        val directionLabel = if (direction == AlertDirection.ABOVE) "dépasse" else "descend sous"
                        "Alerte quand ${metal.displayNameFr} $directionLabel $targetLabel"
                    }
                }
                if (previewText != null) {
                    Text(
                        previewText,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted44Dark,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (percent != null && percent > 0.0 && currentPriceUsdPerGram != null) {
                        onConfirm(metal, direction, percent, currency)
                    }
                },
            ) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}
