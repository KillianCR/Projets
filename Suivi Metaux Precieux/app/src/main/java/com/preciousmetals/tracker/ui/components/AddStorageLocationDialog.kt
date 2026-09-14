package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.util.formatFr
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Shared between Outils (the storage locations list) and Modifier un avoir (so a location can be
 * created without leaving the holding form) — same dialog, same ember styling, one definition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStorageLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, notes: String, reminderDate: LocalDate?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    GlassAlertDialog(
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (reminderDate ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
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
            DatePicker(state = datePickerState)
        }
    }
}
