package com.preciousmetals.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// Primary/CTA accent — electric blue, neobank-reference brief (Revolut/N26/Monzo/Chime): a
// trusted, sober financial-app blue rather than the gold itself, which stays a secondary/metal
// accent below so it keeps doing identity work without competing with every button on screen.
val AccentBlueDark = Color(0xFF5B8CFF)
val OnAccentBlueDark = Color(0xFFF2F3F5)
val AccentBlueLight = Color(0xFF3D6FE0)
val OnAccentBlueLight = Color(0xFFFFFFFF)

// Secondary/gold accent — used for emphasis tied to the "Or" identity (secondary role, metal
// badges/charts), not the primary CTA color. Dark text sits on top of it (validated contrast).
val BrandGoldDark = Color(0xFFD4AF37)
val BrandGoldLight = Color(0xFFB8860B)
val OnBrandGold = Color(0xFF1B1B1F)

// Dark theme — charcoal, not pure black, per the neobank-reference brief. surface and
// surfaceVariant are deliberately two DIFFERENT shades (not both the brief's single "card"
// value): giving two roles the exact same Color makes Material3's contentColorFor() — which
// matches container colors by exact value — resolve to the wrong role's "on" color (this bit
// the app once already, see errorContainer below).
val BackgroundDark = Color(0xFF101114)
val SurfaceDark = Color(0xFF16171B)
val SurfaceVariantDark = Color(0xFF1B1D22)
val OutlineDark = Color(0xFF26282E)
val OnBackgroundDark = Color(0xFFF2F3F5)
val OnSurfaceVariantDark = Color(0xFF8B8F98)

// Light theme — the brief only specified a dark palette; kept close to the previous warm
// palette rather than guessing a light neobank scheme that was never asked for.
val BackgroundLight = Color(0xFFF7F5F0)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEFEBE2)
val OutlineLight = Color(0xFFE2DDD1)
val OnBackgroundLight = Color(0xFF1B1B1F)
val OnSurfaceVariantLight = Color(0xFF6B6875)

// Status — reserved meaning (gain/loss), never reused as a categorical series color.
val PositiveGreenDark = Color(0xFF3DDC84)
val NegativeRedDark = Color(0xFFFF5C5C)
val PositiveGreenLight = Color(0xFF1E8E3E)
val NegativeRedLight = Color(0xFFD93025)

// Dark error container — deliberately distinct from every other dark-theme role above (see the
// surface/surfaceVariant note): contentColorFor() checks errorContainer before surfaceVariant,
// so reusing another role's value here silently recolors that role's text/icons to error-red.
val ErrorContainerDark = Color(0xFF3A2020)

/**
 * Per-metal categorical colors. Gold and Silver now follow the product design brief's exact
 * "Accent or" / "Accent argent" values. Platinum/Palladium are untouched from the dataviz-skill
 * validated palette (OKLCH lightness band + chroma floor + CVD separation + contrast vs surface)
 * — the brief didn't specify them, so there was nothing to reconcile.
 */
object MetalColors {
    val goldDark = Color(0xFFD4AF37)
    val silverDark = Color(0xFFC7CBD1)
    val platinumDark = Color(0xFF2D74CA)
    val palladiumDark = Color(0xFFC05296)

    val goldLight = Color(0xFF9A7400)
    val silverLight = Color(0xFF00908A)
    val platinumLight = Color(0xFF1467C2)
    val palladiumLight = Color(0xFFB23E88)
}
