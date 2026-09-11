package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.theme.brandColor
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo

/**
 * Horizontally scrollable row of one card per tracked metal: logo, live price/gram and a
 * 7-day sparkline. Sits just below the top bar, above the portfolio stats.
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
                modifier = Modifier.width(128.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    MetalLogo(metal = metal, size = 32.dp)
                    Text(
                        metal.displayNameFr,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = pricePerGram?.let { formatMoney(it.usdTo(currency, usdToEurRate), currency) + "/g" }
                            ?: "…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val spark = sparklineByMetal[metal].orEmpty()
                    if (spark.size >= 2) {
                        Sparkline(
                            values = spark,
                            lineColor = color,
                            height = 28.dp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
