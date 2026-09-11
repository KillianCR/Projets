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
    }
}
