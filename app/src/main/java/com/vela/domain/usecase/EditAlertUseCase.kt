package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AlertRepository
import com.vela.domain.repository.DetailRepository
import javax.inject.Inject

class EditAlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
    private val detailRepository: DetailRepository
) {
    suspend operator fun invoke(alert: Alert) {
        val anchorTimestamp = fetchLastCandleTimestamp(alert.coinId)
        if (alert.id == 0L) {
            alertRepository.createAlert(alert.copy(ohlcAnchorTimestamp = anchorTimestamp))
        } else {
            alertRepository.updateAlert(
                alert.copy(
                    isTriggered = false,
                    ohlcAnchorTimestamp = anchorTimestamp
                )
            )
        }
    }

    private suspend fun fetchLastCandleTimestamp(coinId: String): Long = when (val result = detailRepository.getOhlc(coinId, days = 1)) {
        is DataResult.Success -> result.data.lastOrNull()?.timestamp ?: 0L
        is DataResult.Error -> 0L
    }
}
