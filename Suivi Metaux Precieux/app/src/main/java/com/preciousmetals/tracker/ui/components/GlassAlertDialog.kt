package com.preciousmetals.tracker.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.preciousmetals.tracker.ui.theme.CardSurfaceDark

/**
 * Blurs whatever is behind this dialog's window — the real screen content, via the OS compositor.
 * A dialog opens in its own platform [android.view.Window], separate from the app's main window,
 * so Haze (used for the floating pill nav's own blur) can't capture across into it; window
 * blur-behind is the equivalent tool for a separate-window overlay. Android 12+ only — a no-op
 * below that, where the dialog just shows its translucent fill over the normal dim scrim.
 */
@Composable
private fun DialogBlurBehind(radius: Dp = 48.dp) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val view = LocalView.current
    val density = LocalDensity.current
    LaunchedEffect(view, radius) {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@LaunchedEffect
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.attributes = window.attributes.apply {
            blurBehindRadius = with(density) { radius.toPx() }.toInt()
        }
    }
}

/**
 * The app's [AlertDialog], restyled to match the floating pill nav: the same glass-card fill
 * ([CardSurfaceDark]) and corner radius as every kanban card, over a blurred view of the real
 * screen behind it instead of the plain dim scrim.
 */
@Composable
fun GlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        // confirmButton is the one slot every call site always provides (it's non-nullable on the
        // underlying AlertDialog), so it's the reliable place to trigger the blur regardless of
        // which of icon/title/text a given dialog uses.
        confirmButton = {
            DialogBlurBehind()
            confirmButton()
        },
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = CardSurfaceDark,
        properties = properties,
    )
}
