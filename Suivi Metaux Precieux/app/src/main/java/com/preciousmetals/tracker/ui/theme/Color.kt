package com.preciousmetals.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// Secondary/gold accent — used for emphasis tied to the "Or" identity (secondary role, metal
// badges/charts), not the primary CTA color. Dark text sits on top of it (validated contrast).
val BrandGoldDark = Color(0xFFD4AF37)
val OnBrandGold = Color(0xFF1B1B1F)

// The app's shared light text/icon color, used for both onBackground and onSurface (the two
// roles are intentionally identical here — background and surface are too, see NearBlackEmber
// below — so there's no contentColorFor() ambiguity to resolve).
val OnBackgroundDark = Color(0xFFF2F3F5)

// Status — reserved meaning (gain/loss), never reused as a categorical series color.
val PositiveGreenDark = Color(0xFF3DDC84)
val NegativeRedDark = Color(0xFFFF5C5C)

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
// Exact dark text color the "écrans-app.html" mockup puts on every solid-white surface (the
// primary CTA, a selected chip/pill, the selected nav item) — used verbatim, not approximated.
val OnWhiteAccent = Color(0xFF1A0F08)

// Glass-card surface/border — a translucent white composited over the ember background instead
// of an opaque fill (report: "Cartes uniformisées"). Distinct alpha values from each other and
// from every opaque role above, so contentColorFor()'s exact-value matching never collides.
val GlassSurfaceDark = Color(0x14FFFFFF)
val GlassOutlineDark = Color(0x1FFFFFFF)
val OnGlassSurfaceDark = Color(0xFFC7C2BC)

// Exact surface/border/text values from the "écrans-app.html" mockup (Portefeuille/Historique/
// Outils reference) — copied verbatim (#rrggbbaa -> Compose's 0xAARRGGBB) rather than reusing the
// nearby-but-different GlassSurfaceDark/GlassOutlineDark above, since the mockup deliberately uses
// different opacities for cards vs. chips vs. pills vs. inputs.
val CardSurfaceDark = Color(0x0AFFFFFF) // #ffffff0a — card fill
val CardBorderDark = Color(0x1AFFFFFF) // #ffffff1a — card border
val ChipSurfaceDark = Color(0x0DFFFFFF) // #ffffff0d — unselected chip/segment fill
val ChipBorderDark = Color(0x22FFFFFF) // #ffffff22 — unselected chip/segment border
val PillSurfaceDark = Color(0x14FFFFFF) // #ffffff14 — percent badge fill
val EurPillSurfaceDark = Color(0x0FFFFFFF) // #ffffff0f — currency pill fill
val EurPillBorderDark = Color(0x26FFFFFF) // #ffffff26 — currency pill / input field border
val InputSurfaceDark = Color(0x0DFFFFFF) // #ffffff0d — text field fill
val IconTileSurfaceDark = Color(0x10FFFFFF) // #ffffff10 — small icon tile fill
val NavDividerDark = Color(0x14FFFFFF) // #ffffff14 — bottom nav top border
val HeaderIconMutedDark = Color(0x88FFFFFF) // #ffffff88 — refresh/eye icons
val TextMuted80Dark = Color(0xCCFFFFFF) // #ffffffcc
val TextMuted67Dark = Color(0xAAFFFFFF) // #ffffffaa
val TextMuted56Dark = Color(0x90FFFFFF) // #ffffff90
val TextMuted44Dark = Color(0x70FFFFFF) // #ffffff70
val TextMuted38Dark = Color(0x60FFFFFF) // #ffffff60
val TextMuted33Dark = Color(0x55FFFFFF) // #ffffff55

// Metallic gradient stops for the metal logo circles (report: "Logos or / argent").
val GoldGradientLight = Color(0xFFFFF3C4)
val GoldGradientMid = Color(0xFFFFD556)
val GoldGradientDeep = Color(0xFFA6740F)
val SilverGradientLight = Color(0xFFFFFFFF)
val SilverGradientMid = Color(0xFFC9CDD2)
val SilverGradientDeep = Color(0xFF7A8087)

// The mockup's 2-stop gradients for the small per-metal dots (filter-chip leading icon) — lighter
// and more pastel than MetalColors' flat platinum/palladium, which stay unchanged elsewhere
// (chart lines, allocation bars) since the mockup only specifies these for the dot.
val PlatinumDotLight = Color(0xFFCFE0EE)
val PlatinumDotDeep = Color(0xFF4C7AA0)
val PalladiumDotLight = Color(0xFFF3C9DD)
val PalladiumDotDeep = Color(0xFFA45A80)

/**
 * Per-metal categorical colors. Gold and Silver follow the product design brief's exact
 * "Accent or" / "Accent argent" values. Platinum/Palladium are from the dataviz-skill validated
 * palette (OKLCH lightness band + chroma floor + CVD separation + contrast vs surface).
 */
object MetalColors {
    val gold = Color(0xFFD4AF37)
    val silver = Color(0xFFC7CBD1)
    val platinum = Color(0xFF2D74CA)
    val palladium = Color(0xFFC05296)
}
