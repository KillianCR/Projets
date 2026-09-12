package com.preciousmetals.tracker.domain.model

/** Troy ounce — the unit gold/silver/platinum/palladium spot prices are quoted in. */
const val GRAMS_PER_TROY_OUNCE = 31.1034768

/** Avoirdupois pound — the unit COMEX copper futures (this app's copper feed) are quoted in. */
const val GRAMS_PER_POUND = 453.59237

enum class Metal(
    val displayNameFr: String,
    /** Yahoo Finance futures symbol — the source for both live prices and historical backfill. */
    val yahooSymbol: String,
    /** Grams per unit the live/historical feeds quote this metal's raw price in. */
    val apiUnitGrams: Double = GRAMS_PER_TROY_OUNCE,
    /**
     * Grams per unit, and its French label, used for this metal's "big price" display
     * (ticker, historique, alertes) — independent of [apiUnitGrams]: copper is fetched in
     * USD/pound but shown in the more familiar USD/kilo.
     */
    val bigUnitGrams: Double = GRAMS_PER_TROY_OUNCE,
    val bigUnitLabel: String = "once",
) {
    GOLD(displayNameFr = "Or", yahooSymbol = "GC=F"),
    SILVER(displayNameFr = "Argent", yahooSymbol = "SI=F"),
    PLATINUM(displayNameFr = "Platine", yahooSymbol = "PL=F"),
    PALLADIUM(displayNameFr = "Palladium", yahooSymbol = "PA=F"),
    COPPER(
        displayNameFr = "Cuivre",
        yahooSymbol = "HG=F",
        apiUnitGrams = GRAMS_PER_POUND,
        bigUnitGrams = 1000.0,
        bigUnitLabel = "kilo",
    ),
}
