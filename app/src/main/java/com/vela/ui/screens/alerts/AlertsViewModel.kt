package com.vela.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.usecase.DeleteAlertUseCase
import com.vela.domain.usecase.GetPricesAndMarketDataUseCase
import com.vela.domain.usecase.ObserveAllAlertsUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.pricepolling.PricePolling
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.base.pricepolling.PricePollingDelegate
import com.vela.ui.common.components.mapper.formatPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val observeAllAlerts: ObserveAllAlertsUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val alertLabelFormatter: AlertLabelFormatter,
    private val deleteAlert: DeleteAlertUseCase,
    private val getPrices: GetPricesAndMarketDataUseCase,
    private val pricePolling: PricePollingController
) : ViewModel(),
    PricePolling by pricePolling,
    PricePollingDelegate {

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private var coinIds: List<String> = emptyList()

    override fun getIds(): List<String> = coinIds

    override fun onPriceError(error: AppError?) = Unit

    override fun onCleared() {
        pricePolling.cancel()
        super.onCleared()
    }

    init {
        pricePolling.bind(
            scope = viewModelScope,
            getPrices = getPrices,
            delegate = this
        )

        observeAllAlerts()
            .onEach { alerts ->
                when {
                    alerts.isEmpty() -> {
                        coinIds = emptyList()
                        _uiState.value = AlertsUiState.Empty
                    }

                    else -> {
                        val groups = alerts
                            .groupBy { it.coinId }
                            .map { (coinId, coinAlerts) ->
                                val asset = assetPreviewCache.get(coinId)
                                AlertGroupUiModel(
                                    coinId = coinId,
                                    coinName = coinAlerts.first().coinName,
                                    coinSymbol = coinAlerts.first().coinSymbol,
                                    coinImage = asset?.image,
                                    initialPrice = asset?.currentPrice?.let { formatPrice(it) } ?: "",
                                    alerts = coinAlerts.map { alert ->
                                        AlertRowUiModel(
                                            id = alert.id,
                                            label = alertLabelFormatter.format(alert),
                                            isTriggered = alert.isTriggered
                                        )
                                    }
                                )
                            }
                        coinIds = groups.map { it.coinId }
                        _uiState.value = AlertsUiState.Success(groups)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDeleteAlert(id: Long) {
        viewModelScope.launch { deleteAlert(id) }
    }
}
