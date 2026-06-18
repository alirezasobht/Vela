package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject

class EditAlertUseCase
    @Inject
    constructor(
        private val repository: AlertRepository
    ) {
        suspend operator fun invoke(alert: Alert) {
            if (alert.id == 0L) {
                repository.createAlert(alert)
            } else {
                repository.updateAlert(alert)
            }
        }
    }
