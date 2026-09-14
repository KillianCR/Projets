package com.preciousmetals.tracker.work

import android.content.Context
import com.preciousmetals.tracker.data.preferences.UserPreferences
import com.preciousmetals.tracker.data.repository.AlertRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.domain.model.AlertDirection
import kotlinx.coroutines.flow.first

/**
 * Evaluates every enabled price alert against the latest cached price and fires a notification for
 * each one that's crossed its threshold. Shared between the periodic [PriceRefreshWorker] and a
 * manual in-app refresh (e.g. [com.preciousmetals.tracker.ui.dashboard.DashboardViewModel.refresh])
 * — a manual refresh used to only update prices without ever checking alerts, so a threshold
 * crossed by a refresh triggered from inside the app never notified until the next background run.
 */
object AlertChecker {
    suspend fun checkAndNotify(
        context: Context,
        alertRepository: AlertRepository,
        priceRepository: PriceRepository,
        userPreferences: UserPreferences,
    ) {
        if (!userPreferences.notificationsEnabled.first()) return

        for (alert in alertRepository.getEnabledOnce()) {
            val currentPrice = priceRepository.getLatestPriceOnceUsdPerGram(alert.metal) ?: continue
            val triggered = when (alert.direction) {
                AlertDirection.ABOVE -> currentPrice >= alert.thresholdUsdPerGram
                AlertDirection.BELOW -> currentPrice <= alert.thresholdUsdPerGram
            }
            if (triggered) {
                NotificationHelper.showAlertTriggered(context, alert, currentPrice)
                alertRepository.markTriggered(alert, System.currentTimeMillis())
            }
        }
    }
}
