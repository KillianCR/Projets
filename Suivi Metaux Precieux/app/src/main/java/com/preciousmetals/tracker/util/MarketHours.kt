package com.preciousmetals.tracker.util

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

private val NEW_YORK = ZoneId.of("America/New_York")

/**
 * Whether COMEX metals futures (gold/silver/platinum/palladium/copper — the underlying quotes
 * this app's live prices track) are currently trading. They trade nearly around the clock on
 * CME Globex, Sunday 6pm to Friday 5pm New York time, with a daily 5–6pm halt — closed the rest
 * of the time, most notably the whole weekend. Outside these hours a refresh legitimately returns
 * the same price as before: there's no new trade to report, not a bug.
 */
fun isCommodityMarketOpen(now: ZonedDateTime = ZonedDateTime.now(NEW_YORK)): Boolean {
    val et = now.withZoneSameInstant(NEW_YORK)
    val hour = et.hour
    return when (et.dayOfWeek) {
        DayOfWeek.SATURDAY -> false
        DayOfWeek.SUNDAY -> hour >= 18
        DayOfWeek.FRIDAY -> hour < 17
        else -> hour !in 17..17
    }
}
