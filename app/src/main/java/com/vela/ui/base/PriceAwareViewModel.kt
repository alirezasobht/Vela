package com.vela.ui.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
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

abstract class PriceAwareViewModel(
    private val getPrices: GetPricesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val refreshDelayTime: Long = savedStateHandle["delay"] ?: (5 * 60 * 1000L)

    private val _isScreenVisible = MutableStateFlow(false)
    private val _prices = MutableStateFlow<Map<String, SimplePrice?>>(emptyMap())

    private val priceScope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())

    init {
        startPricePolling()
    }

    fun onScreenVisible(visible: Boolean) {
        _isScreenVisible.value = visible
    }

    fun observePrice(id: String): Flow<SimplePriceUiModel?> =
        _prices
            .map { it[id]?.toUiModel() }
            .distinctUntilChanged()

    override fun onCleared() {
        priceScope.cancel()
        super.onCleared()
    }

    protected abstract fun getIdsForPricing(): List<String>
    protected abstract fun onPriceError(error: AppError?)

    private fun startPricePolling() {
        priceScope.launch {
            while (true) {
                delay(refreshDelayTime)
                _isScreenVisible.first { it }
                val ids = getIdsForPricing()
                if (ids.isNotEmpty()) {
                    when (val result = getPrices(ids)) {
                        is DataResult.Success -> {
                            _prices.value = result.data
                            onPriceError(null)
                        }

                        is DataResult.Error -> onPriceError(result.appError)
                    }
                }
            }
        }
    }
}
