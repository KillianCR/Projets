package com.preciousmetals.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.preciousmetals.tracker.data.local.dao.GoalDao
import com.preciousmetals.tracker.data.local.dao.HoldingDao
import com.preciousmetals.tracker.data.local.dao.HoldingDocumentDao
import com.preciousmetals.tracker.data.local.dao.PriceAlertDao
import com.preciousmetals.tracker.data.local.dao.PriceHistoryDao
import com.preciousmetals.tracker.data.local.dao.StorageLocationDao
import com.preciousmetals.tracker.data.local.entity.GoalEntity
import com.preciousmetals.tracker.data.local.entity.HoldingDocumentEntity
import com.preciousmetals.tracker.data.local.entity.HoldingEntity
import com.preciousmetals.tracker.data.local.entity.PriceAlertEntity
import com.preciousmetals.tracker.data.local.entity.PriceHistoryEntity
import com.preciousmetals.tracker.data.local.entity.StorageLocationEntity

@Database(
    entities = [
        HoldingEntity::class,
        PriceHistoryEntity::class,
        PriceAlertEntity::class,
        StorageLocationEntity::class,
        HoldingDocumentEntity::class,
        GoalEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holdingDao(): HoldingDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun storageLocationDao(): StorageLocationDao
    abstract fun holdingDocumentDao(): HoldingDocumentDao
    abstract fun goalDao(): GoalDao

    companion object {
        const val DATABASE_NAME = "suivi_metaux.db"
    }
}
