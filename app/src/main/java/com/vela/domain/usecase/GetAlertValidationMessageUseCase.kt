package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import javax.inject.Inject

sealed class AlertValidationResult {
    data object Valid : AlertValidationResult()
    data object Unchanged : AlertValidationResult()
    data class Invalid(val message: String) : AlertValidationResult()
}

class GetAlertValidationMessageUseCase @Inject constructor() {
    operator fun invoke(
        type: AlertType?,
        direction: AlertDirection?,
        value: String,
        currentPrice: Double? = null,
        originalAlert: Alert? = null
    ): AlertValidationResult {
        if (type == null) return AlertValidationResult.Invalid("Select a type")
        if (direction == null) return AlertValidationResult.Invalid("Select a direction")

        val targetValue = value.toDoubleOrNull()
            ?: return AlertValidationResult.Invalid(
                if (value.isBlank()) "Enter a target value" else "Enter a valid number"
            )

        if (type == AlertType.PRICE) {
            val price = currentPrice
                ?: return AlertValidationResult.Invalid("Current price unavailable")
            val priceValid = when (direction) {
                AlertDirection.ABOVE -> targetValue > price
                AlertDirection.BELOW -> targetValue < price
            }
            if (!priceValid) {
                return AlertValidationResult.Invalid(
                    when (direction) {
                        AlertDirection.ABOVE -> "Target must be above current price"
                        AlertDirection.BELOW -> "Target must be below current price"
                    }
                )
            }
        }

        if (originalAlert != null) {
            val changed = type != originalAlert.type ||
                direction != originalAlert.direction ||
                targetValue != originalAlert.targetValue
            if (!changed) return AlertValidationResult.Unchanged
        }

        return AlertValidationResult.Valid
    }
}
