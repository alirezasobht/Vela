package com.vela.data.platform

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vela.MainActivity
import com.vela.ui.navigation.deeplink.DeepLink
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeepLinkIntentFactory @Inject constructor(@ApplicationContext private val context: Context) {

    fun fromIntent(intent: Intent): DeepLink? = when (intent.getStringExtra(EXTRA_TYPE)) {
        TYPE_COIN_DETAIL -> {
            val coinId = intent.getStringExtra(EXTRA_COIN_ID) ?: return null
            val alertId = intent.getLongExtra(EXTRA_ALERT_ID, -1L).takeIf { it >= 0L }
            DeepLink.CoinDetail(coinId = coinId, alertId = alertId)
        }

        else -> null
    }

    fun buildPendingIntent(
        deepLink: DeepLink,
        requestCode: Int
    ): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        buildIntent(deepLink),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun buildIntent(deepLink: DeepLink): Intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        when (deepLink) {
            is DeepLink.CoinDetail -> {
                putExtra(EXTRA_TYPE, TYPE_COIN_DETAIL)
                putExtra(EXTRA_COIN_ID, deepLink.coinId)
                deepLink.alertId?.let { putExtra(EXTRA_ALERT_ID, it) }
            }
        }
    }

    companion object {
        private const val EXTRA_TYPE = "vela_deeplink_type"
        private const val EXTRA_COIN_ID = "vela_deeplink_coin_id"
        private const val EXTRA_ALERT_ID = "vela_deeplink_alert_id"
        private const val TYPE_COIN_DETAIL = "coin_detail"
    }
}
