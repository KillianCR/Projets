package com.preciousmetals.tracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.GRAMS_PER_TROY_OUNCE
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.theme.brandColor
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo
import kotlinx.coroutines.delay

/**
 * Horizontally scrollable row of one card per tracked metal: logo, live price/gram and
 * price/troy-ounce in two visually separate tiles, a 7-day sparkline, and a brief highlight
 * flash whenever a price ticks to a new value (so a live update is felt, not just quietly
 * swapped in). Sits just below the top bar, above the portfolio stats.
 */
@Composable
fun MetalPriceTicker(
    metals: List<Metal>,
    pricesUsdPerGram: Map<Metal, Double?>,
    sparklineByMetal: Map<Metal, List<Double>>,
    currency: Currency,
    usdToEurRate: Double,
    modifier: Modifier = Modifier,
    onMetalClick: ((Metal) -> Unit)? = null,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(metals, key = { it.name }) { metal ->
            val pricePerGram = pricesUsdPerGram[metal]
            val color = metal.brandColor()
            Card(
                onClick = { onMetalClick?.invoke(metal) },
                enabled = onMetalClick != null,
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.width(156.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    MetalLogo(metal = metal, size = 32.dp)
                    Text(
                        metal.displayNameFr,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    )
                    PriceUnitTile(
                        unitLabel = "gramme",
                        text = pricePerGram?.let { formatMoney(it.usdTo(currency, usdToEurRate), currency) } ?: "…",
                        valueKey = pricePerGram,
                    )
                    PriceUnitTile(
                        unitLabel = "once",
                        text = pricePerGram
                            ?.let { formatMoney((it * GRAMS_PER_TROY_OUNCE).usdTo(currency, usdToEurRate), currency) }
                            ?: "…",
                        valueKey = pricePerGram,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    val spark = sparklineByMetal[metal].orEmpty()
                    if (spark.size >= 2) {
                        Sparkline(
                            values = spark,
                            lineColor = color,
                            height = 28.dp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/** A small, visually distinct tile for one price unit (gram or ounce), inside a metal's card. */
@Composable
private fun PriceUnitTile(
    unitLabel: String,
    text: String,
    valueKey: Double?,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = "Prix / $unitLabel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlashOnChangeText(
                text = text,
                valueKey = valueKey,
                normalColor = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

/** A [Text] that briefly flashes to the theme's primary color when [valueKey] changes value. */
@Composable
private fun FlashOnChangeText(
    text: String,
    valueKey: Double?,
    normalColor: Color,
    modifier: Modifier = Modifier,
) {
    val flashColor = MaterialTheme.colorScheme.primary
    var isFlashing by remember { mutableStateOf(false) }
    var previousKey by remember { mutableStateOf(valueKey) }

    LaunchedEffect(valueKey) {
        val changed = previousKey != null && valueKey != null && previousKey != valueKey
        previousKey = valueKey
        if (changed) {
            isFlashing = true
            delay(700)
            isFlashing = false
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isFlashing) flashColor else normalColor,
        animationSpec = tween(durationMillis = 700),
        label = "priceFlash",
    )

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = animatedColor,
        modifier = modifier,
    )
}
