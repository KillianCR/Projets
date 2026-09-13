package com.preciousmetals.tracker.util

import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH)

fun LocalDate.formatFr(): String = format(dateFormatter)

/** Converts a USD amount to the target display currency using the given USD→EUR rate. */
fun Double.usdTo(currency: Currency, usdToEurRate: Double): Double = when (currency) {
    Currency.USD -> this
    Currency.EUR -> this * usdToEurRate
}

fun formatMoney(amount: Double, currency: Currency): String {
    val rounded = String.format(Locale.FRENCH, "%,.2f", amount)
    return when (currency) {
        Currency.EUR -> "$rounded ${currency.symbol}"
        Currency.USD -> "${currency.symbol}$rounded"
    }
}

fun formatPercent(value: Double): String {
    val sign = if (value > 0) "+" else ""
    return "$sign" + String.format(Locale.FRENCH, "%,.2f", value) + " %"
}

fun formatGrams(grams: Double): String = String.format(Locale.FRENCH, "%,.2f g", grams)

/** A held quantity in the metal's natural small unit (grams for precious metals, kilos for copper). */
fun formatWeight(grams: Double, metal: Metal): String {
    val quantity = grams / metal.smallUnitGrams
    val unit = if (metal.smallUnitLabel == "kilo") "kg" else "g"
    return String.format(Locale.FRENCH, "%,.2f %s", quantity, unit)
}
