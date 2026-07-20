package com.vela.domain.notification

import com.vela.domain.model.Alert

interface AlertNotifier {
    companion object {
        const val CHANNEL_ID = "vela_alerts"
    }
    fun notify(alert: Alert)
}
