package com.preciousmetals.tracker.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.preciousmetals.tracker.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided")
}
