package com.preciousmetals.tracker.domain.model

/** Troy ounce — the unit gold/silver/platinum/palladium spot prices are quoted in. */
const val GRAMS_PER_TROY_OUNCE = 31.1034768

/** Avoirdupois pound — the unit COMEX copper futures (this app's copper feed) are quoted in. */
const val GRAMS_PER_POUND = 453.59237

private const val GRAMS_PER_KILO = 1000.0
private const val GRAMS_PER_TONNE = 1_000_000.0

enum class Metal(
    val displayNameFr: String,
    /** Yahoo Finance futures symbol — the source for both live prices and historical backfill. */
    val yahooSymbol: String,
    /** Grams per unit the live/historical feeds quote this metal's raw price in. */
    val apiUnitGrams: Double = GRAMS_PER_TROY_OUNCE,
    /**
     * Grams per unit, and its French label, used for the "small price" display (ticker's first
     * tile, historique's second line) — gram for the four precious metals, kilo for copper
     * (nobody prices bulk copper by the gram).
     */
    val smallUnitGrams: Double = 1.0,
    val smallUnitLabel: String = "gramme",
    /**
     * Same idea for the "big price" display (ticker's second tile, historique's headline,
     * alertes) — one tier up from [smallUnitGrams]: troy ounce for the four precious metals,
     * tonne for copper. Independent of [apiUnitGrams], which is only about the feed's raw units.
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
        smallUnitGrams = GRAMS_PER_KILO,
        smallUnitLabel = "kilo",
        bigUnitGrams = GRAMS_PER_TONNE,
        bigUnitLabel = "tonne",
    ),
}
