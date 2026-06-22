package com.vela.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.R
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.EditAlertUseCase
import com.vela.domain.usecase.GetAlertByIdUseCase
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.domain.usecase.ValidateAlertUseCase
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

@HiltViewModel(assistedFactory = AlertFormViewModel.Factory::class)
class AlertFormViewModel @AssistedInject constructor(
    assetPreviewCache: AssetPreviewCache,
    private val getPricesOnly: GetPricesOnlyUseCase,
    private val editAlert: EditAlertUseCase,
    private val getAlertById: GetAlertByIdUseCase,
    private val validateAlert: ValidateAlertUseCase,
    private val alertLabelFormatter: AlertLabelFormatter,
    @Assisted("coinId") val coinId: String,
    @Assisted val alertId: Long?,
    @Assisted("initialLabel") val initialLabel: String?
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("coinId") coinId: String,
            @Assisted alertId: Long?,
            @Assisted("initialLabel") initialLabel: String?
        ): AlertFormViewModel
    }

    private val isEditMode: Boolean = alertId != null

    private val asset: Asset? = assetPreviewCache.get(coinId)

    private var currentPrice: Double? = null
    private var originalAlert: Alert? = null

    private val _uiState = MutableStateFlow(
        AlertFormUiState(
            editBtnResId = if (isEditMode) R.string.action_edit_alert else R.string.action_create_alert,
            alertLabel = initialLabel
        )
    )
    val uiState: StateFlow<AlertFormUiState> = _uiState.asStateFlow()

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
        _uiState.update { it.copy(type = type).withSubmitEnabled() }
    }

    fun onDirectionSelected(direction: AlertDirection) {
        _uiState.update { it.copy(direction = direction).withSubmitEnabled() }
    }

    fun onValueChanged(value: String) {
        _uiState.update { it.copy(value = value).withSubmitEnabled() }
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
            currentPrice = (result as? DataResult.Success)?.data?.get(coinId)?.price
            _uiState.update {
                it.copy(
                    value = currentPrice?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: "",
                    isValueInputEnabled = true
                ).withSubmitEnabled()
            }
        }
    }

    private fun loadAlert() {
        viewModelScope.launch {
            val alert = alertId?.let { getAlertById(it) }
            if (alert != null) {
                originalAlert = alert
                val priceResult = getPricesOnly(listOf(coinId))
                currentPrice = (priceResult as? DataResult.Success)?.data?.get(coinId)?.price
                _uiState.update {
                    it.copy(
                        type = alert.type,
                        direction = alert.direction,
                        value = alert.targetValue.toBigDecimal().stripTrailingZeros().toPlainString(),
                        isValueInputEnabled = true
                    ).withSubmitEnabled()
                }
            } else {
                _dismissEvent.send(Unit)
            }
        }
    }

    private fun AlertFormUiState.withSubmitEnabled(): AlertFormUiState {
        val isValid = validateAlert(
            type = type,
            direction = direction,
            value = value,
            currentPrice = currentPrice,
            originalAlert = originalAlert
        )
        val label = if (type != null && direction != null && value.toDoubleOrNull() != null) {
            alertLabelFormatter.format(
                Alert(
                    id = alertId ?: 0L,
                    coinId = coinId,
                    coinName = asset?.name ?: "",
                    coinSymbol = asset?.symbol ?: "",
                    type = type,
                    direction = direction,
                    targetValue = value.toDouble()
                )
            )
        } else {
            null
        }
        return copy(isSubmitEnabled = isValid, alertLabel = label)
    }
}
