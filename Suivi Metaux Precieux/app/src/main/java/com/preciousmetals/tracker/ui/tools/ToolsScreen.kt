package com.preciousmetals.tracker.ui.tools

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.Goal
import com.preciousmetals.tracker.domain.model.GoalTargetType
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.StorageLocation
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.GlassChip
import com.preciousmetals.tracker.ui.components.GlassSegmentedRow
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.ui.components.glassInputFieldColors
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.theme.TextMuted33Dark
import com.preciousmetals.tracker.ui.theme.TextMuted38Dark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.util.formatFr
import com.preciousmetals.tracker.util.formatGrams
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.formatWeight
import com.preciousmetals.tracker.util.usdTo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onLocationClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val viewModel: ToolsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ToolsViewModel(
                    goalRepository = container.goalRepository,
                    storageLocationRepository = container.storageLocationRepository,
                    holdingRepository = container.holdingRepository,
                    priceRepository = container.priceRepository,
                    userPreferences = container.userPreferences,
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Outils") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp, top = 16.dp, bottom = bottomNavContentPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item { CalculatorSection(state = state) }

            item { DcaSimulatorSection(state = state) }

            item {
                GoalsSection(
                    state = state,
                    onAddClick = { showAddGoalDialog = true },
                    onDelete = viewModel::deleteGoal,
                )
            }

            item {
                StorageLocationsSection(
                    state = state,
                    onAddClick = { showAddLocationDialog = true },
                    onDelete = viewModel::deleteStorageLocation,
                    onClick = { location -> onLocationClick(location.id) },
                )
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { label, metal, type, amount ->
                viewModel.addGoal(label, metal, type, amount)
                showAddGoalDialog = false
            },
        )
    }

    if (showAddLocationDialog) {
        AddStorageLocationDialog(
            onDismiss = { showAddLocationDialog = false },
            onConfirm = { name, notes, reminderDate ->
                viewModel.addStorageLocation(
                    StorageLocation(name = name, notes = notes, insuranceReminderDate = reminderDate)
                )
                showAddLocationDialog = false
            },
        )
    }
}

@Composable
private fun ToolSectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
}

// ---------------------------------------------------------------------------------------------
// Calculatrice (poids + pureté -> valeur estimée)
// ---------------------------------------------------------------------------------------------

private data class PurityOption(val label: String, val fraction: Double)

private fun purityOptionsFor(metal: Metal): List<PurityOption> = when (metal) {
    Metal.GOLD -> listOf(
        PurityOption("24K (999)", 0.999),
        PurityOption("22K (916)", 0.916),
        PurityOption("18K (750)", 0.750),
        PurityOption("14K (585)", 0.585),
        PurityOption("9K (375)", 0.375),
    )
    Metal.SILVER -> listOf(
        PurityOption("Fin (999)", 0.999),
        PurityOption("Sterling (925)", 0.925),
        PurityOption("800", 0.800),
    )
    Metal.PLATINUM, Metal.PALLADIUM -> listOf(
        PurityOption("999", 0.999),
        PurityOption("950", 0.950),
    )
    Metal.COPPER -> listOf(
        PurityOption("Pur (999)", 0.999),
        PurityOption("Électrolytique (999,9)", 0.9999),
    )
}

@Composable
private fun CalculatorSection(state: ToolsUiState) {
    var metal by remember { mutableStateOf(Metal.GOLD) }
    val purityOptions = purityOptionsFor(metal)
    var purity by remember(metal) { mutableStateOf(purityOptions.first()) }
    var weightText by remember { mutableStateOf("") }

    val pricePerGram = state.livePricesUsdPerGram[metal]?.usdTo(state.currency, state.usdToEurRate)
    val enteredWeight = weightText.replace(',', '.').toDoubleOrNull()
    val weightGrams = enteredWeight?.times(metal.smallUnitGrams)
    val estimatedValue = if (pricePerGram != null && weightGrams != null && weightGrams > 0.0) {
        pricePerGram * purity.fraction * weightGrams
    } else {
        null
    }

    Column {
        ToolSectionTitle("Calculatrice")
        Text(
            "Estime la valeur d'un objet selon son poids et sa pureté.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
            color = TextMuted44Dark,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp),
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

                Text(
                    "Pureté",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted44Dark,
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                ) {
                    items(purityOptions, key = { it.label }) { option ->
                        GlassChip(
                            selected = purity == option,
                            onClick = { purity = option },
                            label = option.label,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    placeholder = { Text("Poids (${metal.smallUnitLabel}s)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassInputFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                )

                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 16.dp),
                )

                Text(
                    "VALEUR ESTIMÉE",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
                    color = TextMuted38Dark,
                )
                Text(
                    estimatedValue?.let { formatMoney(it, state.currency) } ?: "—",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp, letterSpacing = (-0.5).sp, fontFeatureSettings = "tnum"),
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (pricePerGram != null) {
                    val unitSuffix = if (metal.smallUnitLabel == "kilo") "kg" else "g"
                    Text(
                        "${formatMoney(pricePerGram * purity.fraction * metal.smallUnitGrams, state.currency)}/$unitSuffix à ${purity.label}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextMuted33Dark,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Simulateur DCA (achat programmé)
// ---------------------------------------------------------------------------------------------

@Composable
private fun DcaSimulatorSection(state: ToolsUiState) {
    var metal by remember { mutableStateOf(Metal.GOLD) }
    var monthlyAmountText by remember { mutableStateOf("") }
    var targetGramsText by remember { mutableStateOf("") }

    val pricePerGram = state.livePricesUsdPerGram[metal]?.usdTo(state.currency, state.usdToEurRate)
    val monthlyAmount = monthlyAmountText.replace(',', '.').toDoubleOrNull()
    val targetGrams = targetGramsText.replace(',', '.').toDoubleOrNull()?.times(metal.smallUnitGrams)

    val monthsNeeded = if (pricePerGram != null && pricePerGram > 0.0 && monthlyAmount != null && monthlyAmount > 0.0 && targetGrams != null && targetGrams > 0.0) {
        val gramsPerMonth = monthlyAmount / pricePerGram
        ceil(targetGrams / gramsPerMonth).toInt()
    } else {
        null
    }

    Column {
        ToolSectionTitle("Simulateur d'achat programmé (DCA)")
        Text(
            "Combien de temps pour atteindre un objectif de poids en investissant un montant fixe chaque mois.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
            color = TextMuted44Dark,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
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
                OutlinedTextField(
                    value = monthlyAmountText,
                    onValueChange = { monthlyAmountText = it },
                    placeholder = { Text("Montant mensuel (${state.currency.symbol})") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassInputFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
                OutlinedTextField(
                    value = targetGramsText,
                    onValueChange = { targetGramsText = it },
                    placeholder = { Text("Objectif de poids (${metal.smallUnitLabel}s)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = glassInputFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                if (monthsNeeded != null) {
                    val years = monthsNeeded / 12
                    val months = monthsNeeded % 12
                    val durationText = when {
                        years > 0 && months > 0 -> "$years an(s) et $months mois"
                        years > 0 -> "$years an(s)"
                        else -> "$monthsNeeded mois"
                    }
                    val unitSuffix = if (metal.smallUnitLabel == "kilo") "kg" else "g"
                    Text(
                        "≈ $durationText pour atteindre ${formatWeight(targetGrams!!, metal)} de ${metal.displayNameFr.lowercase()}, à cours constant (${formatMoney(pricePerGram!! * metal.smallUnitGrams, state.currency)}/$unitSuffix).",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color.White,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                } else if (monthlyAmountText.isNotBlank() || targetGramsText.isNotBlank()) {
                    Text(
                        "Renseignez un montant et un objectif de poids valides.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = TextMuted44Dark,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Objectifs
// ---------------------------------------------------------------------------------------------

@Composable
private fun GoalsSection(state: ToolsUiState, onAddClick: () -> Unit, onDelete: (Goal) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolSectionTitle("Objectifs")
            TextButton(onClick = onAddClick) { Text("Ajouter") }
        }
        if (state.goals.isEmpty()) {
            Text(
                "Aucun objectif. Fixez-vous un cap, par exemple \"500 g d'argent\" ou une valeur totale de portefeuille.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.goals.forEach { progress ->
                    GoalCard(progress = progress, currency = state.currency, usdToEurRate = state.usdToEurRate, onDelete = { onDelete(progress.goal) })
                }
            }
        }
    }
}

@Composable
private fun GoalCard(progress: GoalProgress, currency: com.preciousmetals.tracker.domain.model.Currency, usdToEurRate: Double, onDelete: () -> Unit) {
    val goal = progress.goal
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(goal.label, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    val currentLabel = when (goal.targetType) {
                        GoalTargetType.WEIGHT_GRAMS -> "${formatGrams(progress.currentAmount)} / ${formatGrams(goal.targetAmount)}"
                        GoalTargetType.VALUE_USD -> "${formatMoney(progress.currentAmount.usdTo(currency, usdToEurRate), currency)} / ${formatMoney(goal.targetAmount.usdTo(currency, usdToEurRate), currency)}"
                    }
                    Text(currentLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer l'objectif", tint = MaterialTheme.colorScheme.error)
                }
            }
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )
            Text(
                "${(progress.progressFraction * 100).toInt()} %",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, metal: Metal?, type: GoalTargetType, amount: Double) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(GoalTargetType.WEIGHT_GRAMS) }
    var metal by remember { mutableStateOf(Metal.GOLD) }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel objectif") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nom (ex: Pot d'argent)") },
                    singleLine = true,
                    colors = glassInputFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                GlassSegmentedRow(
                    options = listOf(
                        GoalTargetType.WEIGHT_GRAMS to "Poids d'un métal",
                        GoalTargetType.VALUE_USD to "Valeur totale",
                    ),
                    selected = targetType,
                    onSelect = { targetType = it },
                    modifier = Modifier.padding(top = 12.dp),
                )
                if (targetType == GoalTargetType.WEIGHT_GRAMS) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                        items(Metal.entries, key = { it.name }) { m ->
                            GlassChip(
                                selected = metal == m,
                                onClick = { metal = m },
                                label = m.displayNameFr,
                                leadingContent = { MetalBadge(metal = m) },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (targetType == GoalTargetType.WEIGHT_GRAMS) "Objectif (grammes)" else "Objectif (devise d'affichage)") },
                    singleLine = true,
                    colors = glassInputFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.replace(',', '.').toDoubleOrNull()
                if (label.isNotBlank() && amount != null && amount > 0.0) {
                    onConfirm(label, if (targetType == GoalTargetType.WEIGHT_GRAMS) metal else null, targetType, amount)
                }
            }) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

// ---------------------------------------------------------------------------------------------
// Lieux de stockage
// ---------------------------------------------------------------------------------------------

@Composable
private fun StorageLocationsSection(
    state: ToolsUiState,
    onAddClick: () -> Unit,
    onDelete: (StorageLocation) -> Unit,
    onClick: (StorageLocation) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolSectionTitle("Lieux de stockage")
            TextButton(onClick = onAddClick) { Text("Ajouter") }
        }
        if (state.storageLocations.isEmpty()) {
            Text(
                "Aucun lieu enregistré. Ajoutez vos lieux de stockage (domicile, banque, coffre tiers…) pour les associer à vos avoirs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.storageLocations.forEach { location ->
                    StorageLocationCard(
                        location = location,
                        holdingCount = state.holdingCountByLocationId[location.id] ?: 0,
                        onDelete = { onDelete(location) },
                        onClick = { onClick(location) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageLocationCard(location: StorageLocation, holdingCount: Int, onDelete: () -> Unit, onClick: () -> Unit) {
    val reminderSoon = location.insuranceReminderDate?.let { it.isBefore(LocalDate.now().plusDays(30)) } == true
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(location.name, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Text(
                        "$holdingCount avoir(s)" + if (location.notes.isNotBlank()) " · ${location.notes}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (location.insuranceReminderDate != null) {
                        Text(
                            "Rappel assurance : ${location.insuranceReminderDate.formatFr()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (reminderSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer ce lieu", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStorageLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, notes: String, reminderDate: LocalDate?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau lieu de stockage") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom (ex: Domicile, Banque X)") },
                    singleLine = true,
                    colors = glassInputFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optionnel)") },
                    colors = glassInputFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                TextButton(onClick = { showDatePicker = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Text("  " + (reminderDate?.let { "Rappel assurance : ${it.formatFr()}" } ?: "Ajouter un rappel d'assurance"))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name, notes, reminderDate)
            }) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = (reminderDate ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        reminderDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } },
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}
