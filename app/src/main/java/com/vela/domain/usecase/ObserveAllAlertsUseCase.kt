package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveAllAlertsUseCase @Inject constructor(private val repository: AlertRepository) {
    operator fun invoke(): Flow<List<Alert>> = repository.observeAllAlerts()
}
