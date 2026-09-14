package com.preciousmetals.tracker.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.preciousmetals.tracker.SuiviMetauxApp
import com.preciousmetals.tracker.widget.PortfolioSummaryWidget
import com.preciousmetals.tracker.widget.SpotPriceWidget

/** Periodically refreshes spot prices (building the local history cache) and checks price alerts. */
class PriceRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as SuiviMetauxApp).container

        // refreshAll() reports failure as soon as ANY single metal's request fails on this
        // unofficial, rate-limit-prone feed, but every metal that DID succeed is already cached
        // (see PriceRepository.refreshAll's own per-metal independence). Widgets and alerts must
        // still pick up whatever came through instead of being skipped outright just because one
        // unrelated metal failed on this run — that was silently preventing alerts from ever being
        // checked on any run with a single flaky request.
        val refreshResult = container.priceRepository.refreshAll()

        SpotPriceWidget.refreshAllInstances(applicationContext)
        PortfolioSummaryWidget.refreshAllInstances(applicationContext)

        AlertChecker.checkAndNotify(
            context = applicationContext,
            alertRepository = container.alertRepository,
            priceRepository = container.priceRepository,
            userPreferences = container.userPreferences,
        )

        if (refreshResult.isFailure) {
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
        return Result.success()
    }
}
