package com.vela.domain.usecase

import com.vela.domain.notification.AlertNotifier
import javax.inject.Inject

class RunAlertChecksUseCase @Inject constructor(
    private val checkAlertsByOhlc: CheckAlertsByOhlcUseCase,
    private val checkAlertsByMarketData: CheckAlertsByMarketDataUseCase,
    private val alertNotifier: AlertNotifier
) {
    suspend operator fun invoke() {
        val ohlcTriggered = checkAlertsByOhlc()
        val marketTriggered = checkAlertsByMarketData()
        val triggered = (ohlcTriggered + marketTriggered).distinct()
        triggered.forEach { alertNotifier.notify(it) }
    }
}
