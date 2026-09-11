package com.preciousmetals.tracker.domain.model

/** Once troy ounce, the unit spot prices are always quoted in. */
const val GRAMS_PER_TROY_OUNCE = 31.1034768

enum class Metal(
    val apiSymbol: String,
    val displayNameFr: String,
    /** Yahoo Finance futures symbol, used only to backfill free historical prices. */
    val yahooSymbol: String,
) {
    GOLD(apiSymbol = "XAU", displayNameFr = "Or", yahooSymbol = "GC=F"),
    SILVER(apiSymbol = "XAG", displayNameFr = "Argent", yahooSymbol = "SI=F"),
    PLATINUM(apiSymbol = "XPT", displayNameFr = "Platine", yahooSymbol = "PL=F"),
    PALLADIUM(apiSymbol = "XPD", displayNameFr = "Palladium", yahooSymbol = "PA=F");

    companion object {
        fun fromApiSymbol(symbol: String): Metal? = entries.find { it.apiSymbol == symbol }
    }
}
