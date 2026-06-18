package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAlertsByCoinIdUseCase
    @Inject
    constructor(
        private val repository: AlertRepository
    ) {
        operator fun invoke(coinId: String): Flow<List<Alert>> = repository.observeAlertsByCoinId(coinId)
    }
