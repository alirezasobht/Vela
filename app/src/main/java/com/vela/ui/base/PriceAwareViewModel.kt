package com.vela.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.usecase.ObservePricesUseCase
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class PriceAwareViewModel(
    observePricesUseCase: ObservePricesUseCase
) : ViewModel() {

    protected abstract val assetIdsFlow: Flow<List<String>>

    protected open val priceScope: CoroutineScope get() = viewModelScope

    protected val priceResult: StateFlow<DataResult<Map<String, SimplePrice>>> by lazy {
        observePricesUseCase(assetIdsFlow)
            .stateIn(
                scope = priceScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DataResult.Success(emptyMap())
            )
    }

    fun observePrice(id: String): Flow<SimplePriceUiModel?> =
        priceResult
            .map { result -> (result as? DataResult.Success)?.data?.get(id)?.toUiModel() }
            .distinctUntilChanged()

    protected abstract fun onPriceError(error: AppError?)

    protected fun startPriceErrorObserver() {
        viewModelScope.launch {
            priceResult.collect { result ->
                when (result) {
                    is DataResult.Success -> onPriceError(null)
                    is DataResult.Error -> onPriceError(result.appError)
                }
            }
        }
    }
}
