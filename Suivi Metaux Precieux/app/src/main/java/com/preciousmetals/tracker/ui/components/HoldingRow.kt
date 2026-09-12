package com.preciousmetals.tracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.domain.model.HoldingValuation
import com.preciousmetals.tracker.util.formatFr
import com.preciousmetals.tracker.util.formatWeight

/**
 * One holding row: logo, label/object type/weight/purchase date, current value and gain-loss —
 * the same design used in "Vos avoirs" on the Portefeuille screen and in a storage location's
 * detail screen. Deletion lives on the holding's own edit page (its own confirm dialog), not
 * here — a trash icon on every row was redundant with that.
 */
@Composable
fun HoldingRow(
    valuation: HoldingValuation,
    money: (Double) -> String,
    onClick: () -> Unit,
) {
    val holding = valuation.holding

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "holdingRowScale")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetalLogo(metal = holding.metal, size = 40.dp)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    holding.label.ifBlank { holding.metal.displayNameFr },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${holding.objectType.displayNameFr} · ${formatWeight(holding.grams, holding.metal)} · ${holding.purchaseDate.formatFr()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (valuation.hasLivePrice) money(valuation.currentValueUsd) else "…",
                    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.SemiBold,
                )
                val percent = valuation.gainLossPercent
                if (percent != null) {
                    PercentPill(percent = percent, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
