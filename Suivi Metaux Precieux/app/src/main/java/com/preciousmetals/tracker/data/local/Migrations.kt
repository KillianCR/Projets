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

/**
 * Adds storage locations (with an optional insurance-reminder date), per-holding documents
 * (invoices, certificates…), a nullable storageLocationId on holdings, and savings goals.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE holdings ADD COLUMN storageLocationId INTEGER")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS storage_locations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                notes TEXT NOT NULL,
                insuranceReminderEpochDay INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS holding_documents (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                holdingId INTEGER NOT NULL,
                uri TEXT NOT NULL,
                label TEXT NOT NULL,
                addedAtEpochMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_holding_documents_holdingId ON holding_documents(holdingId)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                label TEXT NOT NULL,
                metal TEXT,
                targetType TEXT NOT NULL,
                targetAmount REAL NOT NULL,
                createdAtEpochMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/**
 * Adds a per-alert display currency, independent of the app's global display currency setting —
 * existing alerts default to EUR, matching the new "Nouvelle alerte" dialog's own default.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE price_alerts ADD COLUMN currency TEXT NOT NULL DEFAULT 'EUR'")
    }
}
