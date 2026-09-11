package com.preciousmetals.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// Brand — a rich, deliberate gold rather than a generic app blue, since the product itself is
// about precious metals. Dark text sits on top of it in both modes (validated >= 5:1 contrast).
val BrandGoldDark = Color(0xFFF2B705)
val BrandGoldLight = Color(0xFFB8860B)
val OnBrandGold = Color(0xFF1B1B1F)

// Dark theme — the signature look (fintech-dark, per the reference design).
val BackgroundDark = Color(0xFF0E0E12)
val SurfaceDark = Color(0xFF1B1B21)
val SurfaceVariantDark = Color(0xFF24242C)
val OutlineDark = Color(0xFF33333D)
val OnBackgroundDark = Color(0xFFF5F3EF)
val OnSurfaceVariantDark = Color(0xFF9A96A8)

// Light theme.
val BackgroundLight = Color(0xFFF7F5F0)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEFEBE2)
val OutlineLight = Color(0xFFE2DDD1)
val OnBackgroundLight = Color(0xFF1B1B1F)
val OnSurfaceVariantLight = Color(0xFF6B6875)

// Status — reserved meaning (gain/loss), never reused as a categorical series color.
val PositiveGreenDark = Color(0xFF34C77B)
val NegativeRedDark = Color(0xFFFF6B6B)
val PositiveGreenLight = Color(0xFF1E8E3E)
val NegativeRedLight = Color(0xFFD93025)

/**
 * Per-metal categorical colors. Validated with the dataviz skill's palette checker
 * (OKLCH lightness band + chroma floor + CVD adjacent/normal-vision separation +
 * contrast vs surface) against surface #1B1B21 (dark) and #FFFFFF (light) — see
 * PR description / commit message for the exact command. Not literal metal colors
 * (e.g. silver isn't a desaturated gray — plain gray fails the chroma floor and stops
 * doing identity work); chosen close to each metal's hue family while staying
 * distinguishable, including under color-vision deficiency.
 */
object MetalColors {
    val goldDark = Color(0xFFA7841D)
    val silverDark = Color(0xFF009993)
    val platinumDark = Color(0xFF2D74CA)
    val palladiumDark = Color(0xFFC05296)

    val goldLight = Color(0xFF9A7400)
    val silverLight = Color(0xFF00908A)
    val platinumLight = Color(0xFF1467C2)
    val palladiumLight = Color(0xFFB23E88)
}
