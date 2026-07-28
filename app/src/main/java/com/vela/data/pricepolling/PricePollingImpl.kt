package com.vela.data.pricepolling

import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.notification.AlertNotifier
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.pricepolling.PricePollingConfig
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.repository.PriceRepository
import com.vela.domain.usecase.CheckAlertsAgainstPricesUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class PricePollingImpl @Inject constructor(
    private val priceRepository: PriceRepository,
    private val priceStore: SimplePriceStore,
    private val config: PricePollingConfig,
    private val checkAlertsAgainstPrices: CheckAlertsAgainstPricesUseCase,
    private val alertNotifier: AlertNotifier,
    @PricePollingScope private val scope: CoroutineScope
) : PricePolling {

    private val refreshDelaySec = config.refreshDelaySeconds.coerceAtLeast(2L)
    private val isScreenVisible = MutableStateFlow(false)
    private val registeredIds = MutableStateFlow<Set<String>>(emptySet())
    private val error = MutableSharedFlow<AppError?>(replay = 1)
    private var pollingJob: Job? = null

    override fun onScreenVisible(visible: Boolean) {
        isScreenVisible.value = visible
    }

    override fun register(ids: Set<String>) {
        if (ids.isEmpty()) return
        registeredIds.update { it + ids }
        scope.launch { fetchPrices() }
        schedulePoll()
    }

    override fun observeError(): SharedFlow<AppError?> = error

    private fun schedulePoll() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (true) {
                delay(refreshDelaySec.seconds)
                isScreenVisible.first { it }
                fetchPrices()
            }
        }
    }

    private suspend fun fetchPrices() {
        val ids = registeredIds.value.toList()
        if (ids.isEmpty()) return
        when (val result = priceRepository.getPrices(ids, includeMarketData = true)) {
            is DataResult.Success -> {
                priceStore.upsert(result.data)
                error.emit(null)
                checkAlertsAgainstPrices().forEach { alertNotifier.notify(it) }
            }

            is DataResult.Error -> error.emit(result.appError)
        }
    }
}
