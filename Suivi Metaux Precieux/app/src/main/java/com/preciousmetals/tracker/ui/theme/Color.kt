package com.preciousmetals.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// Primary/CTA accent for the light theme — the "braise" redesign only specifies a dark palette
// (see below), so light theme keeps this earlier electric-blue accent unchanged.
val AccentBlueLight = Color(0xFF3D6FE0)
val OnAccentBlueLight = Color(0xFFFFFFFF)

// Secondary/gold accent — used for emphasis tied to the "Or" identity (secondary role, metal
// badges/charts), not the primary CTA color. Dark text sits on top of it (validated contrast).
val BrandGoldDark = Color(0xFFD4AF37)
val BrandGoldLight = Color(0xFFB8860B)
val OnBrandGold = Color(0xFF1B1B1F)

// Dark theme's shared light text/icon color, used for both onBackground and onSurface (the two
// roles are intentionally identical here — background and surface are too, see NearBlackEmber
// below — so there's no contentColorFor() ambiguity to resolve).
val OnBackgroundDark = Color(0xFFF2F3F5)

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

// "Braise" redesign — radial ember background + unified white accent (redesign report:
// Rapport_de_refonte___Mon_portefeuille). The five stops replicate, verbatim, the report's CSS:
// radial-gradient(circle at 15% 50%, #8a3410 0%, #5c220c 22%, #2c1108 44%, #100907 68%, #070504 100%)
val EmberOrange = Color(0xFF8A3410)
val RustBrown = Color(0xFF5C220C)
val DeepBrown = Color(0xFF2C1108)
val NearBlackEmber = Color(0xFF100907)
val BlackEmber = Color(0xFF070504)

// Unified accent — every accentuated color (buttons, icons, emphasized amounts) collapses onto
// this single white, at varying opacities, instead of mixing yellow/green/white as before.
val WhiteAccent = Color(0xFFFFFFFF)
val OnWhiteAccent = Color(0xFF1B1B1F)

// Glass-card surface/border — a translucent white composited over the ember background instead
// of an opaque fill (report: "Cartes uniformisées"). Distinct alpha values from each other and
// from every opaque role above, so contentColorFor()'s exact-value matching never collides.
val GlassSurfaceDark = Color(0x14FFFFFF)
val GlassOutlineDark = Color(0x1FFFFFFF)
val OnGlassSurfaceDark = Color(0xFFC7C2BC)

// Metallic gradient stops for the metal logo circles (report: "Logos or / argent").
val GoldGradientLight = Color(0xFFFFF3C4)
val GoldGradientMid = Color(0xFFFFD556)
val GoldGradientDeep = Color(0xFFA6740F)
val SilverGradientLight = Color(0xFFFFFFFF)
val SilverGradientMid = Color(0xFFC9CDD2)
val SilverGradientDeep = Color(0xFF7A8087)

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
