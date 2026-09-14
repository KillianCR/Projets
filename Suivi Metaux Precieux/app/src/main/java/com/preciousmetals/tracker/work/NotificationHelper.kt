package com.preciousmetals.tracker.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.preciousmetals.tracker.R
import com.preciousmetals.tracker.domain.model.AlertDirection
import com.preciousmetals.tracker.domain.model.PriceAlert
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.usdTo

object NotificationHelper {
    private const val CHANNEL_ID = "price_alerts"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_alerts),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_alerts_desc)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * [usdToEurRate] is required to render the notification in [PriceAlert.currency] — the price
     * feed only ever gives USD/gram, so an EUR alert still needs the live exchange rate to show a
     * € amount instead of silently falling back to $.
     */
    fun showAlertTriggered(context: Context, alert: PriceAlert, currentPriceUsdPerGram: Double, usdToEurRate: Double) {
        ensureChannel(context)

        val currentPriceBigUnit = (currentPriceUsdPerGram * alert.metal.bigUnitGrams).usdTo(alert.currency, usdToEurRate)
        val currentPriceSmallUnit = (currentPriceUsdPerGram * alert.metal.smallUnitGrams).usdTo(alert.currency, usdToEurRate)
        val directionText = if (alert.direction == AlertDirection.ABOVE) "a dépassé" else "est descendu sous"
        val title = "${alert.metal.displayNameFr} $directionText votre seuil"
        val text = "Cours actuel : ${formatMoney(currentPriceBigUnit, alert.currency)}/${alert.metal.bigUnitLabel} " +
            "(${formatMoney(currentPriceSmallUnit, alert.currency)}/${alert.metal.smallUnitLabel})"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(alert.id.toInt(), notification)
    }
}
