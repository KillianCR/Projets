package com.preciousmetals.tracker

import android.content.Context
import androidx.room.Room
import com.preciousmetals.tracker.data.export.DataExporter
import com.preciousmetals.tracker.data.local.AppDatabase
import com.preciousmetals.tracker.data.local.MIGRATION_1_2
import com.preciousmetals.tracker.data.local.MIGRATION_2_3
import com.preciousmetals.tracker.data.local.MIGRATION_3_4
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.remote.NetworkModule
import com.preciousmetals.tracker.data.repository.AlertRepository
import com.preciousmetals.tracker.data.repository.GoalRepository
import com.preciousmetals.tracker.data.repository.HoldingDocumentRepository
import com.preciousmetals.tracker.data.repository.HoldingRepository
import com.preciousmetals.tracker.data.repository.PortfolioRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.data.repository.StorageLocationRepository

/** Hand-rolled dependency container (no DI framework) shared by the whole app. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

    val userPreferences = UserPreferences(appContext)

    val priceRepository = PriceRepository(
        exchangeRateApiService = NetworkModule.exchangeRateApiService,
        yahooFinanceApiService = NetworkModule.yahooFinanceApiService,
        priceHistoryDao = database.priceHistoryDao(),
        userPreferences = userPreferences,
    )

    val holdingRepository = HoldingRepository(database.holdingDao())

    val portfolioRepository = PortfolioRepository(holdingRepository, priceRepository)

    val alertRepository = AlertRepository(database.priceAlertDao())

    val storageLocationRepository = StorageLocationRepository(database.storageLocationDao())

    val holdingDocumentRepository = HoldingDocumentRepository(database.holdingDocumentDao())

    val goalRepository = GoalRepository(database.goalDao())

    val dataExporter = DataExporter(holdingRepository)
}
