package com.preciousmetals.tracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * price_history is a rebuildable local cache (refreshed live and backfilled from history), so
 * on this schema change it is simply dropped and recreated with its new unique index — holdings
 * and alerts, the user's actual data, are left untouched.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS price_history")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS price_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                metal TEXT NOT NULL,
                priceUsdPerOunce REAL NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                timestampEpochMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_price_history_metal_dateEpochDay " +
                "ON price_history(metal, dateEpochDay)"
        )
    }
}
