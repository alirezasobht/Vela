package com.vela.ui.base.pricepolling

import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.usecase.GetPricesUseCase
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class PricePollingController
    @Inject
    constructor(
        private val config: PricePollingConfig
    ) : PricePolling {
        private val isScreenVisible = MutableStateFlow(false)
        private val prices = MutableStateFlow<Map<String, SimplePrice?>>(emptyMap())
        private var pollingScope: CoroutineScope? = null

        fun bind(
            scope: CoroutineScope,
            getPrices: GetPricesUseCase,
            delegate: PricePollingDelegate
        ) {
            cancel()
            pollingScope =
                CoroutineScope(scope.coroutineContext + SupervisorJob()).apply {
                    val refreshDelaySec = config.refreshDelaySeconds.coerceAtLeast(2L)
                    launch {
                        while (true) {
                            delay(refreshDelaySec.seconds)
                            isScreenVisible.first { it }
                            val ids = delegate.getIds()
                            if (ids.isNotEmpty()) {
                                when (val result = getPrices(ids)) {
                                    is DataResult.Success -> {
                                        prices.value = result.data
                                        delegate.onPriceError(null)
                                    }

                                    is DataResult.Error -> delegate.onPriceError(result.appError)
                                }
                            }
                        }
                    }
                }
        }

        override fun onScreenVisible(visible: Boolean) {
            isScreenVisible.value = visible
        }

        override fun observePrice(id: String): Flow<SimplePriceUiModel?> =
            prices
                .map { it[id]?.toUiModel() }
                .distinctUntilChanged()

        fun cancel() {
            pollingScope?.cancel()
            pollingScope = null
        }
    }
