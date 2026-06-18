package com.vela.domain.usecase

import com.vela.domain.repository.AlertRepository
import javax.inject.Inject

class DeleteAlertUseCase
    @Inject
    constructor(
        private val repository: AlertRepository
    ) {
        suspend operator fun invoke(id: Long) = repository.deleteAlert(id)
    }
