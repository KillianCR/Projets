package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo

/**
 * Horizontally scrollable row of one card per tracked metal, showing its logo and live
 * price per gram. Sits just below the top bar, above the portfolio stats.
 */
@Composable
fun MetalPriceTicker(
    metals: List<Metal>,
    pricesUsdPerGram: Map<Metal, Double?>,
    currency: Currency,
    usdToEurRate: Double,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
    ) {
        items(metals, key = { it.name }) { metal ->
            val pricePerGram = pricesUsdPerGram[metal]
            Card(modifier = Modifier.width(104.dp)) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MetalLogo(metal = metal)
                    Text(
                        metal.displayNameFr,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = pricePerGram?.let { formatMoney(it.usdTo(currency, usdToEurRate), currency) + "/g" }
                            ?: "…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
