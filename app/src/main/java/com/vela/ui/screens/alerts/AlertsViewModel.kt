package com.vela.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.usecase.ObserveAllAlertsUseCase
import com.vela.ui.base.AssetPreviewCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val observeAllAlerts: ObserveAllAlertsUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val alertLabelFormatter: AlertLabelFormatter
) : ViewModel() {
    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        observeAllAlerts()
            .onEach { alerts ->
                when {
                    alerts.isEmpty() -> _uiState.value = AlertsUiState.Empty

                    else -> {
                        val groups = alerts
                            .groupBy { it.coinId }
                            .map { (coinId, coinAlerts) ->
                                AlertGroupUiModel(
                                    coinId = coinId,
                                    coinName = coinAlerts.first().coinName,
                                    coinSymbol = coinAlerts.first().coinSymbol,
                                    coinImage = assetPreviewCache.get(coinId)?.image,
                                    alerts = coinAlerts.map { alert ->
                                        AlertRowUiModel(
                                            id = alert.id,
                                            label = alertLabelFormatter.format(alert),
                                            isTriggered = alert.isTriggered
                                        )
                                    }
                                )
                            }
                        _uiState.value = AlertsUiState.Success(groups)
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}