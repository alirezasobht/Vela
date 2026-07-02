package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AlertRepository
import com.vela.domain.repository.DetailRepository
import javax.inject.Inject

class CheckAlertsByOhlcUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
    private val detailRepository: DetailRepository
) {
    suspend operator fun invoke(): List<Alert> {
        val activeAlerts = alertRepository.getActiveAlertsSnapshot()
            .filter { it.type == AlertType.PRICE }

        val triggered = mutableListOf<Alert>()

        for (alert in activeAlerts) {
            val lowerBound = maxOf(alert.ohlcAnchorTimestamp, alert.lastOhlcCheckTimestamp)

            val candles = when (val result = detailRepository.getOhlc(alert.coinId, days = 1)) {
                is DataResult.Success -> result.data
                is DataResult.Error -> continue
            }

            val newCandles = candles.filter { it.timestamp > lowerBound }
            if (newCandles.isEmpty()) continue

            val isTriggered = newCandles.any { candle ->
                when (alert.direction) {
                    AlertDirection.ABOVE -> candle.high >= alert.targetValue
                    AlertDirection.BELOW -> candle.low <= alert.targetValue
                }
            }

            if (isTriggered) {
                alertRepository.markTriggered(alert.id)
                triggered += alert
            } else {
                alertRepository.updateOhlcCheckTimestamp(alert.id, newCandles.last().timestamp)
            }
        }

        return triggered
    }
}
