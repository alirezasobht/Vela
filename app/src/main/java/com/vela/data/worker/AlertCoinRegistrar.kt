package com.vela.data.worker

import com.vela.data.pricepolling.PricePollingScope
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.usecase.ObserveAllAlertsUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Singleton
class AlertCoinRegistrar @Inject constructor(
    private val observeAllAlerts: ObserveAllAlertsUseCase,
    private val pricePolling: PricePolling,
    @PricePollingScope private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = observeAllAlerts()
            .onEach { alerts ->
                val ids = alerts.map { it.coinId }.toSet()
                if (ids.isNotEmpty()) pricePolling.register(ids)
            }
            .launchIn(scope)
    }
}
