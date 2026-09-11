package com.preciousmetals.tracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.addholding.AddEditHoldingScreen
import com.preciousmetals.tracker.ui.alerts.AlertsScreen
import com.preciousmetals.tracker.ui.dashboard.DashboardScreen
import com.preciousmetals.tracker.ui.history.PriceHistoryScreen
import com.preciousmetals.tracker.ui.settings.SettingsScreen

private val bottomTabs = listOf(
    BottomTab(Destinations.DASHBOARD, "Portefeuille", Icons.Filled.Dashboard),
    BottomTab(Destinations.HISTORY, "Historique", Icons.Filled.ShowChart),
    BottomTab(Destinations.ALERTS, "Alertes", Icons.Filled.Notifications),
    BottomTab(Destinations.SETTINGS, "Réglages", Icons.Filled.Settings),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute } ||
        currentRoute == Destinations.HISTORY_FOR_METAL_PATTERN

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val isHistoryTab = tab.route == Destinations.HISTORY
                        NavigationBarItem(
                            selected = currentRoute == tab.route ||
                                (isHistoryTab && currentRoute == Destinations.HISTORY_FOR_METAL_PATTERN),
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destinations.DASHBOARD) {
                DashboardScreen(
                    onAddHolding = { navController.navigate(Destinations.addHolding()) },
                    onEditHolding = { id -> navController.navigate(Destinations.editHolding(id)) },
                    onMetalClick = { metal -> navController.navigate(Destinations.historyForMetal(metal)) },
                )
            }
            composable(Destinations.HISTORY) {
                PriceHistoryScreen()
            }
            composable(
                route = Destinations.HISTORY_FOR_METAL_PATTERN,
                arguments = listOf(navArgument(Destinations.HISTORY_METAL_ARG) { type = NavType.StringType }),
            ) { entry ->
                val metal = entry.arguments?.getString(Destinations.HISTORY_METAL_ARG)
                    ?.let { name -> runCatching { Metal.valueOf(name) }.getOrNull() }
                PriceHistoryScreen(initialMetal = metal)
            }
            composable(Destinations.ALERTS) {
                AlertsScreen()
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = Destinations.ADD_EDIT_HOLDING_PATTERN,
                arguments = listOf(
                    navArgument(Destinations.HOLDING_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
            ) { entry ->
                val holdingId = entry.arguments?.getLong(Destinations.HOLDING_ID_ARG) ?: -1L
                AddEditHoldingScreen(
                    holdingId = holdingId.takeIf { it >= 0L },
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
