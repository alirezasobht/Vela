package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import javax.inject.Inject

class ValidateAlertUseCase @Inject constructor() {
    operator fun invoke(
        type: AlertType?,
        direction: AlertDirection?,
        value: String,
        currentPrice: Double? = null,
        originalAlert: Alert? = null
    ): Boolean {
        val targetValue = value.toDoubleOrNull() ?: return false
        if (type == null || direction == null) return false

        if (type == AlertType.PRICE) {
            val price = currentPrice ?: return false
            val priceValid = when (direction) {
                AlertDirection.ABOVE -> targetValue > price
                AlertDirection.BELOW -> targetValue < price
            }
            if (!priceValid) return false
        }

        if (originalAlert != null) {
            val changed = type != originalAlert.type ||
                direction != originalAlert.direction ||
                targetValue != originalAlert.targetValue
            if (!changed) return false
        }

        return true
    }
}
