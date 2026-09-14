package com.preciousmetals.tracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.CompactTopBar
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.GlassChip
import com.preciousmetals.tracker.ui.components.horizontalFadingEdges
import com.preciousmetals.tracker.ui.components.topFadingEdge
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.theme.TextMuted33Dark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.util.formatFr
import com.preciousmetals.tracker.work.WorkScheduler
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // canAuthenticate() does IPC to the system biometric service and can take 100-300ms — called
    // synchronously in remember{}, this used to block composition of the whole screen (and, with
    // it, every other animation running on the same UI thread, including the nav bar's pill) for
    // that entire time. Running it in a coroutine keeps that latency off the composition/frame
    // pipeline entirely; the switch just starts disabled for a moment instead of freezing the app.
    var biometricAvailable by remember { mutableStateOf(false) }
    LaunchedEffect(context) {
        biometricAvailable = withContext(Dispatchers.Default) {
            BiometricManager.from(context).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { CompactTopBar(title = "Réglages") },
        // CompactTopBar and this screen's own bottom padding already own the top/bottom system-bar
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
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSectionTitle("Devise d'affichage")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Currency.entries.forEach { currency ->
                            GlassChip(
                                selected = state.currency == currency,
                                onClick = { viewModel.setCurrency(currency) },
                                label = currency.code,
                            )
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSectionTitle("Fréquence d'actualisation des cours")
                    val refreshListState = rememberLazyListState()
                    LazyRow(
                        state = refreshListState,
                        modifier = Modifier.horizontalFadingEdges(refreshListState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 10.dp),
                    ) {
                        items(refreshOptions, key = { it.first }) { (minutes, label) ->
                            GlassChip(
                                selected = state.refreshIntervalMinutes == minutes,
                                onClick = { viewModel.setRefreshIntervalMinutes(minutes) },
                                label = label,
                            )
                        }
                    }
                    val lastRefresh = state.lastRefreshEpochMillis
                    if (lastRefresh != null) {
                        val date = Instant.ofEpochMilli(lastRefresh).atZone(ZoneId.systemDefault()).toLocalDate()
                        Text(
                            "Dernière actualisation : ${date.formatFr()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted44Dark,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        SettingsSectionTitle("Notifications d'alertes")
                        Text(
                            "Être notifié quand un cours atteint un seuil défini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted44Dark,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        SettingsSectionTitle("Verrouillage biométrique")
                        Text(
                            if (biometricAvailable) {
                                "Empreinte, visage ou code de l'appareil requis à l'ouverture de l'app"
                            } else {
                                "Configurez une empreinte, un visage ou un code sur cet appareil pour " +
                                    "activer cette option"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted44Dark,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Switch(
                        checked = state.appLockEnabled,
                        enabled = biometricAvailable,
                        onCheckedChange = { viewModel.setAppLockEnabled(it) },
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSectionTitle("Historique des cours")
                    Text(
                        "Recharge ~5 ans de cours quotidiens (source gratuite Yahoo Finance, sans " +
                            "compte) pour permettre le calcul automatique du prix d'achat même sur " +
                            "un achat ancien. Se fait normalement tout seul au premier lancement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted44Dark,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
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

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSectionTitle("Sauvegarde")
                    Text(
                        "Vos données restent uniquement sur cet appareil. Exportez-les régulièrement " +
                            "pour les sauvegarder ou les transférer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted44Dark,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
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
                color = TextMuted33Dark,
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}
