package com.preciousmetals.tracker.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.preciousmetals.tracker.SuiviMetauxApp
import com.preciousmetals.tracker.domain.model.AlertDirection
import com.preciousmetals.tracker.widget.SpotPriceWidget
import kotlinx.coroutines.flow.first

/** Periodically refreshes spot prices (building the local history cache) and checks price alerts. */
class PriceRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as SuiviMetauxApp).container

        val refreshResult = container.priceRepository.refreshAll()
        if (refreshResult.isFailure) {
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }

        SpotPriceWidget.refreshAllInstances(applicationContext)

        val notificationsEnabled = container.userPreferences.notificationsEnabled.first()
        if (notificationsEnabled) {
            val enabledAlerts = container.alertRepository.getEnabledOnce()
            for (alert in enabledAlerts) {
                val currentPrice = container.priceRepository.getLatestPriceOnceUsdPerGram(alert.metal) ?: continue
                val triggered = when (alert.direction) {
                    AlertDirection.ABOVE -> currentPrice >= alert.thresholdUsdPerGram
                    AlertDirection.BELOW -> currentPrice <= alert.thresholdUsdPerGram
                }
                if (triggered) {
                    NotificationHelper.showAlertTriggered(applicationContext, alert, currentPrice)
                    container.alertRepository.markTriggered(alert, System.currentTimeMillis())
                }
            }
        }

        return Result.success()
    }
}
