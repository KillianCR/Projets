package com.preciousmetals.tracker

import android.app.Application
import com.preciousmetals.tracker.domain.model.Metal
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
            // Best-effort throughout: on failure (e.g. transient rate limiting), leave the flag
            // unset so it's retried on a later launch instead of surfacing an error to the user.
            if (!container.userPreferences.historicalBackfillDone.first()) {
                val result = container.priceRepository.backfillHistoricalPrices()
                if (result.isSuccess) {
                    container.userPreferences.setHistoricalBackfillDone(true)
                    container.userPreferences.setCopperBackfillDone(true)
                }
            } else if (!container.userPreferences.copperBackfillDone.first()) {
                // An install that already finished the full backfill before Copper existed —
                // catch up just that one metal instead of never getting its chart history.
                val result = container.priceRepository.backfillHistoricalPrices(metals = listOf(Metal.COPPER))
                if (result.isSuccess) {
                    container.userPreferences.setCopperBackfillDone(true)
                }
            }
        }
    }
}
