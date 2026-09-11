package com.preciousmetals.tracker

import android.content.Context
import androidx.room.Room
import com.preciousmetals.tracker.data.export.DataExporter
import com.preciousmetals.tracker.data.local.AppDatabase
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.remote.NetworkModule
import com.preciousmetals.tracker.data.repository.AlertRepository
import com.preciousmetals.tracker.data.repository.HoldingRepository
import com.preciousmetals.tracker.data.repository.PortfolioRepository
import com.preciousmetals.tracker.data.repository.PriceRepository

/** Hand-rolled dependency container (no DI framework) shared by the whole app. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    ).build()

    val userPreferences = UserPreferences(appContext)

    val priceRepository = PriceRepository(
        goldApiService = NetworkModule.goldApiService,
        exchangeRateApiService = NetworkModule.exchangeRateApiService,
        priceHistoryDao = database.priceHistoryDao(),
        userPreferences = userPreferences,
    )

    val holdingRepository = HoldingRepository(database.holdingDao())

    val portfolioRepository = PortfolioRepository(holdingRepository, priceRepository)

    val alertRepository = AlertRepository(database.priceAlertDao())

    val dataExporter = DataExporter(holdingRepository)
}
