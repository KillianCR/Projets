package com.preciousmetals.tracker.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
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
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.PriceAlert
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.CompactTopBar
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.GlassChip
import com.preciousmetals.tracker.ui.components.GlassSegmentedRow
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.ui.components.glassInputFieldColors
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.navigation.bottomNavOverlayPadding
import com.preciousmetals.tracker.ui.theme.NearBlackEmber
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
                    userPreferences = container.userPreferences,
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
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomNavContentPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.alerts, key = { it.id }) { alert ->
                    AlertRow(
                        alert = alert,
                        currencyLabel = formatMoney(
                            (alert.thresholdUsdPerGram * alert.metal.smallUnitGrams).usdTo(state.currency, state.usdToEurRate),
                            state.currency,
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
            onDismiss = { showAddDialog = false },
            onConfirm = { metal, direction, thresholdEur, perOunce ->
                viewModel.addAlert(metal, direction, thresholdEur, perOunce)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (Metal, AlertDirection, Double, Boolean) -> Unit,
) {
    var metal by remember { mutableStateOf(Metal.GOLD) }
    var direction by remember { mutableStateOf(AlertDirection.ABOVE) }
    var perBigUnit by remember { mutableStateOf(true) }
    var thresholdText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NearBlackEmber,
        title = { Text("Nouvelle alerte") },
        text = {
            Column {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                GlassSegmentedRow(
                    options = listOf(true to "Par ${metal.bigUnitLabel}", false to "Par ${metal.smallUnitLabel}"),
                    selected = perBigUnit,
                    onSelect = { perBigUnit = it },
                    modifier = Modifier.padding(top = 12.dp),
                )
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it },
                    label = { Text("Seuil (€/${if (perBigUnit) metal.bigUnitLabel else metal.smallUnitLabel})") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true,
                    colors = glassInputFieldColors(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = thresholdText.replace(',', '.').toDoubleOrNull()
                if (value != null && value > 0.0) {
                    onConfirm(metal, direction, value, perBigUnit)
                }
            }) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}
