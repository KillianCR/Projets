package com.preciousmetals.tracker

import android.app.Application
import com.preciousmetals.tracker.work.NotificationHelper
import com.preciousmetals.tracker.work.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SuiviMetauxApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)

        applicationScope.launch {
            val intervalMinutes = container.userPreferences.refreshIntervalMinutes.first()
            WorkScheduler.schedulePeriodicRefresh(this@SuiviMetauxApp, intervalMinutes)
        }

        applicationScope.launch {
            if (!container.userPreferences.historicalBackfillDone.first()) {
                val result = container.priceRepository.backfillHistoricalPrices()
                // Best-effort: on failure (e.g. transient rate limiting), leave the flag unset
                // so it's retried on a later launch instead of surfacing an error to the user.
                if (result.isSuccess) {
                    container.userPreferences.setHistoricalBackfillDone(true)
                }
            }
        }
    }
}
