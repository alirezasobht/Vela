package com.vela.domain.notification

import com.vela.domain.model.Alert

interface AlertNotifier {
    fun notify(alert: Alert)
}
