package com.preciousmetals.tracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

/**
 * A rounded, floating icon-only nav bar (selected item gets a filled brand-color pill behind
 * its icon) rather than the flat, full-bleed Material3 NavigationBar — closer to the reference
 * fintech design than the default component.
 */
@Composable
fun FloatingBottomNav(
    tabs: List<BottomTab>,
    isSelected: (BottomTab) -> Boolean,
    onSelect: (BottomTab) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(68.dp),
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    NavPill(tab = tab, selected = isSelected(tab), onClick = { onSelect(tab) })
                }
            }
        }
    }
}

@Composable
private fun NavPill(tab: BottomTab, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .height(48.dp)
            .background(background, CircleShape)
            .then(
                if (selected) Modifier.padding(horizontal = 18.dp) else Modifier.size(48.dp)
            ),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (selected) 20.dp else 24.dp)) {
            Icon(imageVector = tab.icon, contentDescription = tab.label, tint = contentColor)
        }
        if (selected) {
            Text(
                text = tab.label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
