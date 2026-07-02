package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AlertRepository
import com.vela.domain.repository.PriceRepository
import javax.inject.Inject

class CheckAlertsByMarketDataUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
    private val priceRepository: PriceRepository
) {
    suspend operator fun invoke(): List<Alert> {
        val activeAlerts = alertRepository.getActiveAlertsSnapshot()
        if (activeAlerts.isEmpty()) return emptyList()

        val coinIds = activeAlerts.map { it.coinId }.distinct()
        val prices = when (val result = priceRepository.getPrices(coinIds, includeMarketData = true)) {
            is DataResult.Success -> result.data
            is DataResult.Error -> return emptyList()
        }

        val triggered = mutableListOf<Alert>()

        for (alert in activeAlerts) {
            val simplePrice = prices[alert.coinId] ?: continue

            val value = when (alert.type) {
                AlertType.PRICE -> simplePrice.price
                AlertType.PERCENT -> simplePrice.priceChange
            } ?: continue

            val isTriggered = when (alert.direction) {
                AlertDirection.ABOVE -> value >= alert.targetValue
                AlertDirection.BELOW -> value <= alert.targetValue
            }

            if (isTriggered) {
                alertRepository.markTriggered(alert.id)
                triggered += alert
            }
        }

        return triggered
    }
}
