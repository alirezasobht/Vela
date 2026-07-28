package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject

class CheckAlertsAgainstPricesUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
    private val priceStore: SimplePriceStore,
    private val alertTriggerEvaluator: AlertTriggerEvaluator
) {
    suspend operator fun invoke(): List<Alert> {
        val prices = priceStore.getPrices()
        if (prices.isEmpty()) return emptyList()

        val activeAlerts = alertRepository.getActiveAlerts()
        if (activeAlerts.isEmpty()) return emptyList()

        val triggered = alertTriggerEvaluator.evaluate(activeAlerts, prices)
        triggered.forEach { alertRepository.markTriggered(it.id) }
        return triggered
    }
}
