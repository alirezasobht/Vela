package com.vela.ui.screens.alerts

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

class AlertLabelFormatter @Inject constructor() {

    private val priceFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 10
        isGroupingUsed = true
    }

    fun format(alert: Alert): String = format(alert.direction, alert.targetValue, alert.type)

    fun format(
        direction: AlertDirection,
        value: Double,
        type: AlertType
    ): String {
        val directionLabel = when (direction) {
            AlertDirection.ABOVE -> "above"
            AlertDirection.BELOW -> "below"
        }
        return when (type) {
            AlertType.PRICE -> "Price $directionLabel \$${priceFormat.format(value)}"
            AlertType.PERCENT -> "Price change $directionLabel ${value.toBigDecimal().stripTrailingZeros().toPlainString()}%"
        }
    }
}
