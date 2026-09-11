package com.preciousmetals.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.preciousmetals.tracker.data.local.dao.HoldingDao
import com.preciousmetals.tracker.data.local.dao.PriceAlertDao
import com.preciousmetals.tracker.data.local.dao.PriceHistoryDao
import com.preciousmetals.tracker.data.local.entity.HoldingEntity
import com.preciousmetals.tracker.data.local.entity.PriceAlertEntity
import com.preciousmetals.tracker.data.local.entity.PriceHistoryEntity

@Database(
    entities = [HoldingEntity::class, PriceHistoryEntity::class, PriceAlertEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holdingDao(): HoldingDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun priceAlertDao(): PriceAlertDao

    companion object {
        const val DATABASE_NAME = "suivi_metaux.db"
    }
}
