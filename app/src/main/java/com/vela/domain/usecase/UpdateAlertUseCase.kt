package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject

class UpdateAlertUseCase
    @Inject
    constructor(
        private val repository: AlertRepository
    ) {
        suspend operator fun invoke(alert: Alert) = repository.updateAlert(alert)
    }
