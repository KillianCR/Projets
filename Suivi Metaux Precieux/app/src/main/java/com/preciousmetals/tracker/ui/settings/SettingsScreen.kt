package com.preciousmetals.tracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.util.formatFr
import com.preciousmetals.tracker.work.WorkScheduler
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay

private val refreshOptions = listOf(60 to "1 h", 180 to "3 h", 360 to "6 h", 720 to "12 h", 1440 to "24 h")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    userPreferences = container.userPreferences,
                    dataExporter = container.dataExporter,
                    priceRepository = container.priceRepository,
                    onRefreshIntervalChanged = { minutes -> WorkScheduler.schedulePeriodicRefresh(context, minutes) },
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.let { viewModel.exportData(it) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.let { viewModel.importData(it) }
        }
    }
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.let { viewModel.exportDataCsv(it) }
        }
    }

    val biometricAvailable = remember(context) {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Réglages") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = bottomNavContentPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Devise d'affichage", style = MaterialTheme.typography.titleMedium)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Currency.entries.forEachIndexed { index, currency ->
                            SegmentedButton(
                                selected = state.currency == currency,
                                onClick = { viewModel.setCurrency(currency) },
                                shape = SegmentedButtonDefaults.itemShape(index, Currency.entries.size),
                            ) { Text(currency.code) }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fréquence d'actualisation des cours", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        refreshOptions.forEach { (minutes, label) ->
                            OutlinedButton(onClick = { viewModel.setRefreshIntervalMinutes(minutes) }) {
                                Text(if (state.refreshIntervalMinutes == minutes) "✓ $label" else label)
                            }
                        }
                    }
                    val lastRefresh = state.lastRefreshEpochMillis
                    if (lastRefresh != null) {
                        val date = Instant.ofEpochMilli(lastRefresh).atZone(ZoneId.systemDefault()).toLocalDate()
                        Text(
                            "Dernière actualisation : ${date.formatFr()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Notifications d'alertes", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Être notifié quand un cours atteint un seuil défini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Verrouillage biométrique", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (biometricAvailable) {
                                "Empreinte, visage ou code de l'appareil requis à l'ouverture de l'app"
                            } else {
                                "Configurez une empreinte, un visage ou un code sur cet appareil pour " +
                                    "activer cette option"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.appLockEnabled,
                        enabled = biometricAvailable,
                        onCheckedChange = { viewModel.setAppLockEnabled(it) },
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Historique des cours", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Recharge ~5 ans de cours quotidiens (source gratuite Yahoo Finance, sans " +
                            "compte) pour permettre le calcul automatique du prix d'achat même sur " +
                            "un achat ancien. Se fait normalement tout seul au premier lancement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    )
                    if (state.isBackfillingHistory) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    } else {
                        OutlinedButton(onClick = { viewModel.backfillHistory() }) {
                            Text("Recharger l'historique")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sauvegarde", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Vos données restent uniquement sur cet appareil. Exportez-les régulièrement " +
                            "pour les sauvegarder ou les transférer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = { exportLauncher.launch("suivi-metaux-export.json") }) {
                            Text("Exporter (JSON)")
                        }
                        OutlinedButton(onClick = { exportCsvLauncher.launch("suivi-metaux-export.csv") }) {
                            Text("Exporter (CSV)")
                        }
                        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                            Text("Importer")
                        }
                    }
                }
            }

            if (state.message != null) {
                Text(state.message!!, color = MaterialTheme.colorScheme.primary)
                LaunchedEffect(state.message) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessage()
                }
            }

            Text(
                "Les cours en direct et l'historique proviennent de Yahoo Finance, et le taux de " +
                    "change EUR/USD de la Banque centrale européenne (frankfurter.app). " +
                    "Deux sources gratuites, sans compte ni clé API.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
