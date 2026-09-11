package com.preciousmetals.tracker.domain.model

/** Once troy ounce, the unit spot prices are always quoted in. */
const val GRAMS_PER_TROY_OUNCE = 31.1034768

enum class Metal(val apiSymbol: String, val displayNameFr: String, val colorHex: Long) {
    GOLD(apiSymbol = "XAU", displayNameFr = "Or", colorHex = 0xFFD4AF37),
    SILVER(apiSymbol = "XAG", displayNameFr = "Argent", colorHex = 0xFFB0B7BE),
    PLATINUM(apiSymbol = "XPT", displayNameFr = "Platine", colorHex = 0xFF7E93A5),
    PALLADIUM(apiSymbol = "XPD", displayNameFr = "Palladium", colorHex = 0xFFCFC1E0);

    companion object {
        fun fromApiSymbol(symbol: String): Metal? = entries.find { it.apiSymbol == symbol }
    }
}
