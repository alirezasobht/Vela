package com.vela.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.usecase.DeleteAlertUseCase
import com.vela.domain.usecase.ObserveAllAlertsUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.common.components.mapper.formatPrice
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val observeAllAlerts: ObserveAllAlertsUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val alertLabelFormatter: AlertLabelFormatter,
    private val deleteAlert: DeleteAlertUseCase,
    private val priceStore: SimplePriceStore,
    private val pricePolling: PricePolling
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    fun onScreenVisible(visible: Boolean) {
        pricePolling.onScreenVisible(visible)
    }

    fun observePrice(id: String): Flow<SimplePriceUiModel?> = priceStore.observePrice(id).map { it?.toUiModel() }

    init {
        observeAllAlerts()
            .onEach { alerts ->
                when {
                    alerts.isEmpty() -> {
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
                        pricePolling.register(groups.map { it.coinId }.toSet())
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
