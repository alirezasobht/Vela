package com.vela.ui.screens.alerts

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import javax.inject.Inject

class AlertLabelFormatter @Inject constructor() {

    fun format(alert: Alert): String = format(alert.direction, alert.targetValue, alert.type)

    fun format(
        direction: AlertDirection,
        value: Double,
        type: AlertType
    ): String {
        val direction = when (direction) {
            AlertDirection.ABOVE -> "above"
            AlertDirection.BELOW -> "below"
        }
        val value = value.toBigDecimal().stripTrailingZeros().toPlainString()
        return when (type) {
            AlertType.PRICE -> "Price $direction \$$value"
            AlertType.PERCENT -> "Price change $direction $value%"
        }
    }
}
