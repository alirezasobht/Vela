package com.vela.data.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vela.R
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.notification.AlertNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

class AlertNotificationSender @Inject constructor(
    @ApplicationContext private val context: Context
) : AlertNotifier {

    private val priceFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 10
        isGroupingUsed = true
    }

    private val percentFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 10
        isGroupingUsed = false
    }

    override fun notify(alert: Alert) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${alert.coinName} (${alert.coinSymbol.uppercase()})")
            .setContentText(formatBody(alert))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(alert.id.toInt(), notification)
    }

    private fun formatBody(alert: Alert): String {
        val direction = when (alert.direction) {
            AlertDirection.ABOVE -> "above"
            AlertDirection.BELOW -> "below"
        }
        return when (alert.type) {
            AlertType.PRICE -> $$"Price $$direction $$${priceFormat.format(alert.targetValue)}"
            AlertType.PERCENT -> "Price change $direction ${percentFormat.format(alert.targetValue)}%"
        }
    }

    companion object {
        const val CHANNEL_ID = "vela_alerts"
    }
}
