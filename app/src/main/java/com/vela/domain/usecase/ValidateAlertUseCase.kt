package com.vela.domain.usecase

import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import javax.inject.Inject

class ValidateAlertUseCase
    @Inject
    constructor() {
        operator fun invoke(
            type: AlertType?,
            direction: AlertDirection?,
            value: String
        ): Boolean = type != null && direction != null && value.toDoubleOrNull() != null
    }
