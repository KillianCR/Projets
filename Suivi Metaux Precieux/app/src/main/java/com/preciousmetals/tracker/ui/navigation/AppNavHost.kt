package com.preciousmetals.tracker.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
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
import com.preciousmetals.tracker.ui.history.PortfolioHistoryScreen
import com.preciousmetals.tracker.ui.history.PriceHistoryScreen
import com.preciousmetals.tracker.ui.locationdetail.LocationDetailScreen
import com.preciousmetals.tracker.ui.settings.SettingsScreen
import com.preciousmetals.tracker.ui.components.EmberGradientBackground
import com.preciousmetals.tracker.ui.tools.ToolsScreen
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

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
    val showBottomBar = currentRoute == Destinations.MAIN ||
        currentRoute == Destinations.HISTORY_FOR_METAL_PATTERN ||
        currentRoute == Destinations.LOCATION_DETAIL_PATTERN ||
        currentRoute == Destinations.PORTFOLIO_HISTORY

    // Which of the 5 tabs is showing, inside the single Destinations.MAIN destination — switching
    // tabs is just flipping this, never a real NavHost transaction (see TabHost doc for why).
    var selectedTab by rememberSaveable { mutableStateOf(Destinations.DASHBOARD) }

    val hazeState = rememberHazeState(blurEnabled = true)

    Box(modifier = Modifier.fillMaxSize()) {
        // The pill's backdrop blur only has something to blur if the captured source actually
        // PAINTS pixels. Every screen here is transparent over the ember gradient MainActivity
        // paints further up, so marking them as the source captured nothing and the blur silently
        // did nothing. Painting the same gradient again inside the hazeSource node (identical, so
        // no visual change) gives the blur a real backdrop — and keeps the source a SIBLING of
        // FloatingBottomNav below, never its ancestor, which is what the working demo does.
        EmberGradientBackground(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
            Scaffold(
                containerColor = Color.Transparent,
                // Purely a layout container here — every destination it hosts already handles its
                // own system-bar insets (CompactTopBar's statusBarsPadding, bottomNavContentPadding /
                // bottomNavOverlayPadding for the floating pill's clearance), so this must not add
                // any of its own or those would double up now that edge-to-edge is enabled.
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Destinations.MAIN,
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
                    exitTransition = { fadeOut(tween(180)) },
                    popEnterTransition = { fadeIn(tween(220)) },
                    popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 8 } },
                ) {
                    composable(Destinations.MAIN) {
                        TabHost(
                            selectedTabRoute = selectedTab,
                            onAddHolding = { navController.navigate(Destinations.addHolding()) },
                            onEditHolding = { id -> navController.navigate(Destinations.editHolding(id)) },
                            onMetalClick = { metal -> navController.navigate(Destinations.historyForMetal(metal)) },
                            onLocationClick = { locationId -> navController.navigate(Destinations.locationDetail(locationId)) },
                            onViewStorageLocations = { navController.navigate(Destinations.locationDetail()) },
                            onViewPortfolioHistory = { navController.navigate(Destinations.PORTFOLIO_HISTORY) },
                        )
                    }
                    composable(Destinations.PORTFOLIO_HISTORY) {
                        PortfolioHistoryScreen(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = Destinations.HISTORY_FOR_METAL_PATTERN,
                        arguments = listOf(navArgument(Destinations.HISTORY_METAL_ARG) { type = NavType.StringType }),
                    ) { entry ->
                        val metal = entry.arguments?.getString(Destinations.HISTORY_METAL_ARG)
                            ?.let { name -> runCatching { Metal.valueOf(name) }.getOrNull() }
                        PriceHistoryScreen(initialMetal = metal)
                    }
                    composable(
                        route = Destinations.LOCATION_DETAIL_PATTERN,
                        arguments = listOf(
                            navArgument(Destinations.LOCATION_ID_ARG) {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        ),
                    ) { entry ->
                        val locationId = entry.arguments?.getLong(Destinations.LOCATION_ID_ARG) ?: -1L
                        LocationDetailScreen(
                            locationId = locationId.takeIf { it >= 0L },
                            onEditHolding = { id -> navController.navigate(Destinations.editHolding(id)) },
                            onBack = { navController.popBackStack() },
                        )
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

        if (showBottomBar) {
            FloatingBottomNav(
                tabs = bottomTabs,
                isSelected = { tab ->
                    // These two detail routes are pushed on top of MAIN without touching
                    // selectedTab (e.g. the portfolio's own "Stockage" shortcut reaches
                    // LOCATION_DETAIL_PATTERN while selectedTab is still "dashboard") — checking
                    // them first keeps the pill pinned to the one tab that route belongs to,
                    // instead of ALSO matching selectedTab's now-stale tab underneath it.
                    // Lieux de stockage counts as Portefeuille, not Outils — it's reached from,
                    // and returns to, the portfolio now. Historique du portefeuille is the same:
                    // reached from the portfolio's own quick action, pinned there too.
                    when (currentRoute) {
                        Destinations.HISTORY_FOR_METAL_PATTERN -> tab.route == Destinations.HISTORY
                        Destinations.LOCATION_DETAIL_PATTERN,
                        Destinations.PORTFOLIO_HISTORY -> tab.route == Destinations.DASHBOARD
                        else -> selectedTab == tab.route
                    }
                },
                onSelect = { tab ->
                    // From an argument-route detail screen (per-metal history, a storage
                    // location's detail, the portfolio's own value history) pushed on top of
                    // MAIN, pop back to it first.
                    if (currentRoute == Destinations.HISTORY_FOR_METAL_PATTERN ||
                        currentRoute == Destinations.LOCATION_DETAIL_PATTERN ||
                        currentRoute == Destinations.PORTFOLIO_HISTORY
                    ) {
                        navController.popBackStack()
                    }
                    selectedTab = tab.route
                },
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/**
 * Renders all 5 bottom tabs at once, permanently — only [selectedTabRoute]'s is visible and
 * interactive, the other 4 sit hidden (alpha 0, touch-blocked) behind it. This is what actually
 * fixes the freeze two separate slow-motion recordings traced back to NavHost: with the normal
 * "one composable() per tab" setup, leaving a tab disposes its whole Compose UI tree, so coming
 * back means rebuilding it from scratch (ViewModel state survives via saveState/restoreState, but
 * every Icon/Text/Row in the screen still has to be recomposed, measured, laid out and drawn
 * again) — on a content-heavy tab (the portfolio's ticker, holdings, allocation chart) that cold
 * rebuild was enough to stall the UI thread the nav bar's pill animation runs on too. Here every
 * tab's composition — and with it scroll position, in-progress form state, everything — simply
 * stays alive the whole time; switching tabs costs nothing more than an alpha flip.
 */
@Composable
private fun TabHost(
    selectedTabRoute: String,
    onAddHolding: () -> Unit,
    onEditHolding: (Long) -> Unit,
    onMetalClick: (Metal) -> Unit,
    onLocationClick: (Long) -> Unit,
    onViewStorageLocations: () -> Unit,
    onViewPortfolioHistory: () -> Unit,
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    Box(modifier = Modifier.fillMaxSize()) {
        bottomTabs.forEach { tab ->
            val isCurrent = tab.route == selectedTabRoute
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (isCurrent) 1f else 0f }
                    .zIndex(if (isCurrent) 1f else 0f)
                    .then(if (isCurrent) Modifier else Modifier.blockPointerInput()),
            ) {
                saveableStateHolder.SaveableStateProvider(tab.route) {
                    when (tab.route) {
                        Destinations.DASHBOARD -> DashboardScreen(
                            onAddHolding = onAddHolding,
                            onEditHolding = onEditHolding,
                            onMetalClick = onMetalClick,
                            onViewStorageLocations = onViewStorageLocations,
                            onViewPortfolioHistory = onViewPortfolioHistory,
                        )
                        Destinations.HISTORY -> PriceHistoryScreen()
                        Destinations.ALERTS -> AlertsScreen()
                        Destinations.TOOLS -> ToolsScreen(onLocationClick = onLocationClick)
                        Destinations.SETTINGS -> SettingsScreen()
                    }
                }
            }
        }
    }
}

/** Swallows every pointer event before it reaches this subtree's own content — keeps a hidden
 * tab's buttons/lists from responding to touches meant for the visible tab drawn above it. */
private fun Modifier.blockPointerInput(): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}
