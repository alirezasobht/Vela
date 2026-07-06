package com.vela.domain.usecase

import javax.inject.Inject

class RunAlertChecksUseCase @Inject constructor(
    private val checkAlertsByOhlc: CheckAlertsByOhlcUseCase,
    private val checkAlertsByMarketData: CheckAlertsByMarketDataUseCase
) {
    suspend operator fun invoke() {
        val ohlcTriggered = checkAlertsByOhlc()
        val marketTriggered = checkAlertsByMarketData()
        val triggered = (ohlcTriggered + marketTriggered).distinct()
        // TODO: fire notifications for triggered alerts
    }
}
