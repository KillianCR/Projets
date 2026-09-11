package com.preciousmetals.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Reserved gain/loss colors — never reused as a per-metal categorical color. */
@Composable
fun positiveColor(): Color = if (isSystemInDarkTheme()) PositiveGreenDark else PositiveGreenLight

@Composable
fun negativeColor(): Color = if (isSystemInDarkTheme()) NegativeRedDark else NegativeRedLight
