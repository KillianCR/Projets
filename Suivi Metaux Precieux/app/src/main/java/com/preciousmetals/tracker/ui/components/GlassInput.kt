package com.preciousmetals.tracker.ui.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.preciousmetals.tracker.ui.theme.EurPillBorderDark
import com.preciousmetals.tracker.ui.theme.InputSurfaceDark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark

/**
 * The mockup's filled-translucent text field look (bg #ffffff0d, border #ffffff26) instead of
 * Material3's default outline style — shared by every screen with free-text input (Outils,
 * Alertes, Réglages) so fields look the same everywhere instead of each screen styling its own.
 */
@Composable
fun glassInputFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = InputSurfaceDark,
    unfocusedContainerColor = InputSurfaceDark,
    focusedBorderColor = EurPillBorderDark,
    unfocusedBorderColor = EurPillBorderDark,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = TextMuted44Dark,
    unfocusedLabelColor = TextMuted44Dark,
    focusedPlaceholderColor = TextMuted44Dark,
    unfocusedPlaceholderColor = TextMuted44Dark,
)
