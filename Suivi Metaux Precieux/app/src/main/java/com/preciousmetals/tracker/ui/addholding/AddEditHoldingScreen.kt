package com.preciousmetals.tracker.ui.addholding

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.preciousmetals.tracker.domain.model.HoldingDocument
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.ObjectType
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.MetalBadge
import com.preciousmetals.tracker.util.formatFr
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHoldingScreen(
    holdingId: Long?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: AddEditHoldingViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AddEditHoldingViewModel(
                    holdingId = holdingId,
                    holdingRepository = container.holdingRepository,
                    priceRepository = container.priceRepository,
                    storageLocationRepository = container.storageLocationRepository,
                    holdingDocumentRepository = container.holdingDocumentRepository,
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val storageLocations by viewModel.storageLocations.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onDone()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.setPhotoUri(uri.toString()) }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val label = queryDisplayName(context, uri) ?: "Document"
            viewModel.addDocument(uri.toString(), label)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Modifier l'avoir" else "Ajouter un avoir") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer")
                        }
                    }
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(Icons.Outlined.Check, contentDescription = "Enregistrer")
                    }
                }
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Métal", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp),
                ) {
                    items(Metal.entries, key = { it.name }) { metal ->
                        FilterChip(
                            selected = state.metal == metal,
                            onClick = { viewModel.setMetal(metal) },
                            label = { Text(metal.displayNameFr) },
                            leadingIcon = { MetalBadge(metal = metal) },
                        )
                    }
                }
            }

            item {
                Text("Type d'objet", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ObjectType.entries.forEach { type ->
                        FilterChip(
                            selected = state.objectType == type,
                            onClick = { viewModel.setObjectType(type) },
                            label = { Text(type.displayNameFr) },
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.gramsText,
                    onValueChange = viewModel::setGramsText,
                    label = { Text("Poids (${state.metal.smallUnitLabel}s)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            item {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Text("  Date d'achat : ${state.purchaseDate.formatFr()}")
                }
            }

            item {
                OutlinedTextField(
                    value = state.label,
                    onValueChange = viewModel::setLabel,
                    label = { Text("Nom (ex: Napoléon 20F, Lingot 50g)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.photoUri != null) {
                        AsyncImage(
                            model = state.photoUri,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    OutlinedButton(onClick = {
                        photoPicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                        Text("  Photo (optionnel)")
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (storageLocations.isNotEmpty()) {
                item {
                    Text("Lieu de stockage", style = MaterialTheme.typography.titleMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        FilterChip(
                            selected = state.storageLocationId == null,
                            onClick = { viewModel.setStorageLocation(null) },
                            label = { Text("Aucun") },
                        )
                        storageLocations.forEach { location ->
                            FilterChip(
                                selected = state.storageLocationId == location.id,
                                onClick = { viewModel.setStorageLocation(location.id) },
                                label = { Text(location.name) },
                            )
                        }
                    }
                }
            }

            if (state.isEditing) {
                item {
                    Text("Documents", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Facture, certificat d'authenticité, attestation d'assurance…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                    )
                    documents.forEach { document ->
                        DocumentRow(
                            document = document,
                            onOpen = { openDocument(context, document.uri) },
                            onDelete = { viewModel.deleteDocument(document) },
                        )
                    }
                    OutlinedButton(
                        onClick = { documentPicker.launch(arrayOf("application/pdf", "image/*")) },
                        modifier = Modifier.padding(top = if (documents.isEmpty()) 0.dp else 8.dp),
                    ) {
                        Icon(Icons.Outlined.UploadFile, contentDescription = null)
                        Text("  Ajouter un document")
                    }
                }
            }

            item {
                Text("Prix d'achat", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ValuationMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.valuationMode == mode,
                            onClick = { viewModel.setValuationMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, ValuationMode.entries.size),
                        ) {
                            Text(if (mode == ValuationMode.MANUAL) "Prix payé" else "Calcul auto")
                        }
                    }
                }
            }

            if (state.valuationMode == ValuationMode.MANUAL) {
                item {
                    OutlinedTextField(
                        value = state.pricePaidEurText,
                        onValueChange = viewModel::setPricePaidEurText,
                        label = { Text("Prix payé (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = when {
                                state.historicalPreviewEur != null ->
                                    "Estimation basée sur le cours du ${state.purchaseDate.formatFr()} : " +
                                        String.format("%.2f €", state.historicalPreviewEur)
                                state.historicalPreviewUnavailable ->
                                    "Cours historique indisponible pour cette date (l'app conserve environ " +
                                        "5 ans d'historique). Basculez sur \"Prix payé\" pour saisir le " +
                                        "montant manuellement, ou réessayez plus tard depuis Réglages."
                                else -> "Calcul en cours…"
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (state.error != null) {
                item {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                if (state.isSaving) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = state.purchaseDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.setPurchaseDate(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer cet avoir ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete()
                }) { Text("Supprimer") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun DocumentRow(document: HoldingDocument, onOpen: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            document.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 10.dp).weight(1f),
        )
        IconButton(onClick = onOpen) {
            Icon(Icons.Outlined.UploadFile, contentDescription = "Ouvrir")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Supprimer le document", tint = MaterialTheme.colorScheme.error)
        }
    }
}

private fun openDocument(context: android.content.Context, uriString: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uriString.toUri(), context.contentResolver.getType(uriString.toUri()))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}

/** Best-effort display name for a content:// URI (falls back to null on failure or no column). */
private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? = runCatching {
    context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}.getOrNull()
