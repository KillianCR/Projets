package com.preciousmetals.tracker.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.preciousmetals.tracker.ui.tools.ToolsScreen

private val bottomTabs = listOf(
    BottomTab(Destinations.DASHBOARD, "Portefeuille", Icons.Outlined.AccountBalanceWallet),
    BottomTab(Destinations.HISTORY, "Historique", Icons.AutoMirrored.Outlined.ShowChart),
    BottomTab(Destinations.ALERTS, "Alertes", Icons.Outlined.Notifications),
    BottomTab(Destinations.TOOLS, "Outils", Icons.Outlined.Handyman),
    BottomTab(Destinations.SETTINGS, "Réglages", Icons.Outlined.Settings),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute } ||
        currentRoute == Destinations.HISTORY_FOR_METAL_PATTERN

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destinations.DASHBOARD,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 8 } },
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
                composable(Destinations.TOOLS) {
                    ToolsScreen()
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

        if (showBottomBar) {
            FloatingBottomNav(
                tabs = bottomTabs,
                isSelected = { tab ->
                    currentRoute == tab.route ||
                        (tab.route == Destinations.HISTORY && currentRoute == Destinations.HISTORY_FOR_METAL_PATTERN)
                },
                onSelect = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
