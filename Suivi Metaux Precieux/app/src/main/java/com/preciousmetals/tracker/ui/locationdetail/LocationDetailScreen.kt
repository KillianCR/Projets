package com.preciousmetals.tracker.ui.locationdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.CompactTopBar
import com.preciousmetals.tracker.ui.components.GlassCard
import com.preciousmetals.tracker.ui.components.HoldingRow
import com.preciousmetals.tracker.ui.components.MetalLogo
import com.preciousmetals.tracker.ui.components.horizontalFadingEdges
import com.preciousmetals.tracker.ui.components.topFadingEdge
import com.preciousmetals.tracker.ui.navigation.bottomNavContentPadding
import com.preciousmetals.tracker.ui.theme.TextMuted33Dark
import com.preciousmetals.tracker.ui.theme.TextMuted38Dark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.formatWeight
import com.preciousmetals.tracker.util.usdTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    locationId: Long,
    onEditHolding: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalAppContainer.current
    val viewModel: LocationDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                LocationDetailViewModel(
                    locationId = locationId,
                    portfolioRepository = container.portfolioRepository,
                    storageLocationRepository = container.storageLocationRepository,
                    userPreferences = container.userPreferences,
                    priceRepository = container.priceRepository,
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            CompactTopBar(title = (state as? LocationDetailUiState.Loaded)?.locationName ?: "Lieu de stockage")
        },
    ) { padding ->
        when (val current = state) {
            is LocationDetailUiState.Loading -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is LocationDetailUiState.Loaded -> {
                fun money(usd: Double) = formatMoney(usd.usdTo(current.currency, current.usdToEurRate), current.currency)

                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(padding).fillMaxSize().topFadingEdge(listState),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 16.dp, bottom = bottomNavContentPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "VALEUR TOTALE",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                                fontWeight = FontWeight.Normal,
                                color = TextMuted38Dark,
                            )
                            Text(
                                money(current.totalValueUsd),
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, letterSpacing = (-0.8).sp, fontFeatureSettings = "tnum"),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }

                    if (current.byMetal.isNotEmpty()) {
                        item {
                            val metalListState = rememberLazyListState()
                            LazyRow(
                                state = metalListState,
                                modifier = Modifier.horizontalFadingEdges(metalListState),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(current.byMetal, key = { it.metal.name }) { summary ->
                                    LocationMetalSummaryCard(
                                        metal = summary.metal,
                                        totalValueUsd = summary.totalValueUsd,
                                        totalGrams = summary.totalGrams,
                                        currency = current.currency,
                                        usdToEurRate = current.usdToEurRate,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "AVOIRS DANS CE LIEU",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted44Dark,
                        )
                    }

                    if (current.valuations.isEmpty()) {
                        item {
                            Text(
                                "Aucun avoir associé à ce lieu pour l'instant.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted44Dark,
                            )
                        }
                    } else {
                        items(current.valuations, key = { it.holding.id }) { valuation ->
                            HoldingRow(
                                valuation = valuation,
                                currency = current.currency,
                                money = ::money,
                                onClick = { onEditHolding(valuation.holding.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationMetalSummaryCard(
    metal: Metal,
    totalValueUsd: Double,
    totalGrams: Double,
    currency: Currency,
    usdToEurRate: Double,
) {
    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.width(156.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            MetalLogo(metal = metal, currency = currency, size = 32.dp)
            Text(
                metal.displayNameFr,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
            Column {
                Text(
                    "Valeur totale",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted33Dark,
                )
                Text(
                    formatMoney(totalValueUsd.usdTo(currency, usdToEurRate), currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    "Quantité",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted33Dark,
                )
                Text(
                    formatWeight(totalGrams, metal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}
