package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.SimplePrice
import javax.inject.Inject

class AlertTriggerEvaluator @Inject constructor() {
    fun evaluate(
        alerts: List<Alert>,
        prices: Map<String, SimplePrice>
    ): List<Alert> {
        if (alerts.isEmpty() || prices.isEmpty()) return emptyList()

        val triggered = mutableListOf<Alert>()
        for (alert in alerts) {
            val simplePrice = prices[alert.coinId] ?: continue

            val value = when (alert.type) {
                AlertType.PRICE -> simplePrice.price
                AlertType.PERCENT -> simplePrice.priceChange
            } ?: continue

            val isTriggered = when (alert.direction) {
                AlertDirection.ABOVE -> value >= alert.targetValue
                AlertDirection.BELOW -> value <= alert.targetValue
            }

            if (isTriggered) triggered += alert
        }
        return triggered
    }
}
