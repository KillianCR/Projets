package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A shorter alternative to Material3's TopAppBar for screens that just need a plain title —
 * TopAppBar's own fixed 64dp height plus its status-bar inset padding left a noticeably tall
 * blank gap above the title on every screen using it. This keeps the title (so each screen still
 * reads clearly, matching Portefeuille's own single-line title) but with much tighter padding.
 *
 * [onBack], when set, draws a back arrow before the title (for a screen pushed on top of another
 * without the floating pill nav offering an obvious way back, e.g. Lieux de stockage reached from
 * the portfolio's own shortcut) — the title's own start padding shrinks to match, since the
 * button's touch target already carries that inset.
 */
@Composable
fun CompactTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = if (onBack != null) 4.dp else 20.dp, end = 8.dp, top = 0.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Retour")
                }
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) { actions() }
    }
}
