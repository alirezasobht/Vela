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
        val activeAlerts = alertRepository.getActiveAlerts()
            .filter { it.type == AlertType.PRICE }

        if (activeAlerts.isEmpty()) return emptyList()

        val triggered = mutableListOf<Alert>()

        for ((coinId, alerts) in activeAlerts.groupBy { it.coinId }) {
            val candles = when (val result = detailRepository.getOhlc(coinId, days = 1)) {
                is DataResult.Success -> result.data
                is DataResult.Error -> continue
            }

            for (alert in alerts) {
                val lowerBound = maxOf(alert.ohlcAnchorTimestamp, alert.lastOhlcCheckTimestamp)
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
        }

        return triggered
    }
}
