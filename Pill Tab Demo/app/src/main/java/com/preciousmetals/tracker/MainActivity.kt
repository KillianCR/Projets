package com.preciousmetals.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.preciousmetals.tracker.ui.components.EmberGradientBackground
import com.preciousmetals.tracker.ui.navigation.BottomTab
import com.preciousmetals.tracker.ui.navigation.FloatingBottomNav
import com.preciousmetals.tracker.ui.portfolio.PortfolioScreen
import com.preciousmetals.tracker.ui.theme.SuiviMetauxTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private val DemoTabs = listOf(
    BottomTab("portefeuille", "Portefeuille", Icons.Outlined.AccountBalanceWallet),
    BottomTab("historique", "Historique", Icons.AutoMirrored.Outlined.ShowChart),
    BottomTab("alertes", "Alertes", Icons.Outlined.Notifications),
    BottomTab("outils", "Outils", Icons.Outlined.Handyman),
    BottomTab("reglages", "Réglages", Icons.Outlined.Settings),
)

/**
 * Design sandbox: only "Portefeuille" (imported from Suivi Métaux, with static sample data) has
 * real content — the point of this app is to iterate on the pill tab bar's look without touching
 * or rebuilding the real app. The other four tabs are placeholders, just enough to switch between
 * and see the pill slide.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuiviMetauxTheme {
                val hazeState = remember { HazeState() }
                var selectedTab by remember { mutableStateOf(DemoTabs.first()) }

                // hazeSource and the pill's hazeEffect must be SIBLINGS (as in every Haze sample:
                // the scrollable content and the blurred bar both sit directly in the Scaffold),
                // not source-inside-effect or effect-inside-source — nesting the pill inside the
                // hazeSource subtree (an earlier version of this) meant nothing was actually
                // captured behind it, so it never blurred.
                Box(modifier = Modifier.fillMaxSize()) {
                    EmberGradientBackground(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
                        when (selectedTab.route) {
                            "portefeuille" -> PortfolioScreen(modifier = Modifier.fillMaxSize())
                            else -> PlaceholderScreen(selectedTab.label)
                        }
                    }

                    FloatingBottomNav(
                        tabs = DemoTabs,
                        isSelected = { it == selectedTab },
                        onSelect = { selectedTab = it },
                        hazeState = hazeState,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label — pas implémenté dans la démo", style = MaterialTheme.typography.bodyMedium)
    }
}
