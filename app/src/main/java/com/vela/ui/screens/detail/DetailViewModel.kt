package com.vela.ui.screens.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.TimeRange
import com.vela.domain.usecase.GetCoinDetailUseCase
import com.vela.domain.usecase.GetOhlcUseCase
import com.vela.domain.usecase.GetPricesAndMarketDataUseCase
import com.vela.ui.base.AssetHolder
import com.vela.ui.base.PriceAwareViewModel
import com.vela.ui.navigation.DetailDestination
import com.vela.ui.screens.detail.mapper.toCandleStickModel
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailUiState
import com.vela.ui.screens.detail.state.toCoinDetailUiModel
import com.vela.ui.screens.detail.state.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCoinDetail: GetCoinDetailUseCase,
    private val getOhlc: GetOhlcUseCase,
    private val assetHolder: AssetHolder,
    getFullPrices: GetPricesAndMarketDataUseCase,
    savedStateHandle: SavedStateHandle
) : PriceAwareViewModel(getFullPrices, savedStateHandle) {

    val initialHeader: CoinDetailUiModel?
    private val coinId: String

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    var selectedRange by mutableStateOf(TimeRange.ONE_DAY)
        private set

    override fun getIdsForPricing(): List<String> = listOf(coinId)

    override fun onPriceError(error: AppError?) {
        val current = _uiState.value as? DetailUiState.Success ?: return
        _uiState.value = current.copy(nonBlockingError = error)
    }

    init {
        savedStateHandle.toRoute<DetailDestination>().let { dest ->
            coinId = dest.coinId
            initialHeader = assetHolder.get(coinId)?.toCoinDetailUiModel()
        }
        loadDetail()
    }

    fun retry() = loadDetail()

    fun onRangeSelected(range: TimeRange) {
        selectedRange = range
        loadOhlc(range)
    }

    private fun loadDetail() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            when (val result = getCoinDetail(coinId)) {
                is DataResult.Success -> {
                    _uiState.value = DetailUiState.Success(detail = result.data.toUiModel())
                    loadOhlc(selectedRange)
                }
                is DataResult.Error -> _uiState.value = DetailUiState.Error(result.appError)
            }
        }
    }

    private fun loadOhlc(range: TimeRange) {
        val current = _uiState.value as? DetailUiState.Success ?: return
        _uiState.value = current.copy(isChartLoading = true)
        viewModelScope.launch {
            when (val result = getOhlc(coinId, range)) {
                is DataResult.Success -> {
                    val success = _uiState.value as? DetailUiState.Success ?: return@launch
                    _uiState.value = success.copy(
                        candlestickModelPartial = result.data.toCandleStickModel(),
                        isChartLoading = false
                    )
                }
                is DataResult.Error -> {
                    val success = _uiState.value as? DetailUiState.Success ?: return@launch
                    _uiState.value = success.copy(
                        isChartLoading = false,
                        nonBlockingError = result.appError
                    )
                }
            }
        }
    }
}
