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
import java.util.Locale

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

    fun showAlertTriggered(context: Context, alert: PriceAlert, currentPriceUsdPerGram: Double) {
        ensureChannel(context)

        val currentPriceUsdPerBigUnit = currentPriceUsdPerGram * alert.metal.bigUnitGrams
        val directionText = if (alert.direction == AlertDirection.ABOVE) "a dépassé" else "est descendu sous"
        val title = "${alert.metal.displayNameFr} $directionText votre seuil"
        val text = String.format(
            Locale.FRENCH,
            "Cours actuel : %.2f \$/${alert.metal.bigUnitLabel} (%.2f \$/g)",
            currentPriceUsdPerBigUnit,
            currentPriceUsdPerGram,
        )

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
