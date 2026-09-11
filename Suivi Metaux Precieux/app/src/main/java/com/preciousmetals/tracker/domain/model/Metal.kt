package com.preciousmetals.tracker.domain.model

/** Once troy ounce, the unit spot prices are always quoted in. */
const val GRAMS_PER_TROY_OUNCE = 31.1034768

enum class Metal(
    val apiSymbol: String,
    val displayNameFr: String,
    val colorHex: Long,
    /** Yahoo Finance futures symbol, used only to backfill free historical prices. */
    val yahooSymbol: String,
) {
    GOLD(apiSymbol = "XAU", displayNameFr = "Or", colorHex = 0xFFD4AF37, yahooSymbol = "GC=F"),
    SILVER(apiSymbol = "XAG", displayNameFr = "Argent", colorHex = 0xFFB0B7BE, yahooSymbol = "SI=F"),
    PLATINUM(apiSymbol = "XPT", displayNameFr = "Platine", colorHex = 0xFF7E93A5, yahooSymbol = "PL=F"),
    PALLADIUM(apiSymbol = "XPD", displayNameFr = "Palladium", colorHex = 0xFFCFC1E0, yahooSymbol = "PA=F");

    companion object {
        fun fromApiSymbol(symbol: String): Metal? = entries.find { it.apiSymbol == symbol }
    }
}
