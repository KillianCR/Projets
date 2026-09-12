package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.ui.theme.ChipBorderDark
import com.preciousmetals.tracker.ui.theme.ChipSurfaceDark
import com.preciousmetals.tracker.ui.theme.OnWhiteAccent
import com.preciousmetals.tracker.ui.theme.WhiteAccent

/**
 * The chip/segment look used throughout the mockup (metal picker, purity picker, date-range
 * picker): unselected is a translucent pill with a hairline border and white text, selected is
 * solid white with dark text and no border — replacing Material3's default FilterChip/
 * SegmentedButton, whose stock colors don't match the mockup's exact values.
 */
@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    leadingContent: (@Composable () -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (selected) WhiteAccent else ChipSurfaceDark,
        contentColor = if (selected) OnWhiteAccent else androidx.compose.ui.graphics.Color.White,
        border = if (selected) null else BorderStroke(1.dp, ChipBorderDark),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(contentPadding),
        ) {
            if (leadingContent != null) {
                leadingContent()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 3.5.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

/**
 * A full-width two/multi-option toggle built from [GlassChip]s sharing the row's width equally —
 * the mockup's look for what would otherwise be a Material3 SegmentedButton (whose own default
 * colors don't match: a muted purple selection instead of the app's solid-white pill).
 */
@Composable
fun <T> GlassSegmentedRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            GlassChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = label,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
