package com.vela.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.EditAlertUseCase
import com.vela.domain.usecase.GetAlertByIdUseCase
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.ui.base.AssetPreviewCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AlertEditViewModel.Factory::class)
class AlertEditViewModel
    @AssistedInject
    constructor(
        assetPreviewCache: AssetPreviewCache,
        private val getPricesOnly: GetPricesOnlyUseCase,
        private val editAlert: EditAlertUseCase,
        private val getAlertById: GetAlertByIdUseCase,
        @Assisted val coinId: String,
        @Assisted val alertId: Long?
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(
                coinId: String,
                alertId: Long?
            ): AlertEditViewModel
        }

        val isEditMode: Boolean = alertId != null

        private val asset: Asset? = assetPreviewCache.get(coinId)

        private val _uiState = MutableStateFlow(AlertEditUiState())
        val uiState: StateFlow<AlertEditUiState> = _uiState.asStateFlow()

        private val _dismissEvent = Channel<Unit>(Channel.BUFFERED)
        val dismissEvent = _dismissEvent.receiveAsFlow()

        init {
            if (asset == null) {
                viewModelScope.launch { _dismissEvent.send(Unit) }
            } else if (isEditMode) {
                loadAlert()
            } else {
                fetchInitialPrice()
            }
        }

        fun onTypeSelected(type: AlertType) {
            _uiState.update { it.copy(type = type).withConfirmEnabled() }
        }

        fun onDirectionSelected(direction: AlertDirection) {
            _uiState.update { it.copy(direction = direction).withConfirmEnabled() }
        }

        fun onValueChanged(value: String) {
            _uiState.update { it.copy(value = value).withConfirmEnabled() }
        }

        fun onConfirm() {
            val state = _uiState.value
            val type = state.type ?: return
            val direction = state.direction ?: return
            val targetValue = state.value.toDoubleOrNull() ?: return
            val cachedAsset = asset ?: return

            _uiState.update { it.copy(isLoading = true) }

            viewModelScope.launch {
                editAlert(
                    Alert(
                        id = alertId ?: 0L,
                        coinId = coinId,
                        coinName = cachedAsset.name,
                        coinSymbol = cachedAsset.symbol,
                        type = type,
                        direction = direction,
                        targetValue = targetValue
                    )
                )
                _uiState.update { it.copy(isLoading = false) }
                _dismissEvent.send(Unit)
            }
        }

        private fun fetchInitialPrice() {
            viewModelScope.launch {
                val result = getPricesOnly(listOf(coinId))
                val price = (result as? DataResult.Success)?.data?.get(coinId)?.price
                _uiState.update {
                    it
                        .copy(
                            value = price?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: "",
                            isValueInputEnabled = true
                        ).withConfirmEnabled()
                }
            }
        }

        private fun loadAlert() {
            viewModelScope.launch {
                val alert = alertId?.let { getAlertById(it) }
                if (alert != null) {
                    _uiState.update {
                        it
                            .copy(
                                type = alert.type,
                                direction = alert.direction,
                                value =
                                    alert.targetValue
                                        .toBigDecimal()
                                        .stripTrailingZeros()
                                        .toPlainString(),
                                isValueInputEnabled = true
                            ).withConfirmEnabled()
                    }
                } else {
                    _dismissEvent.send(Unit)
                }
            }
        }

        private fun AlertEditUiState.withConfirmEnabled() =
            copy(
                isConfirmEnabled =
                    type != null &&
                        direction != null &&
                        value.toDoubleOrNull() != null
            )
    }
