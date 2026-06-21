package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject

class GetAlertByIdUseCase @Inject constructor(private val repository: AlertRepository) {
    suspend operator fun invoke(id: Long): Alert? = repository.getAlertById(id)
}
