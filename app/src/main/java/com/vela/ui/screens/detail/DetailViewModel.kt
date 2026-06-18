package com.vela.ui.screens.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.TimeRange
import com.vela.domain.usecase.GetCoinDetailUseCase
import com.vela.domain.usecase.GetOhlcUseCase
import com.vela.domain.usecase.GetPricesAndMarketDataUseCase
import com.vela.domain.usecase.IsWatchlistedUseCase
import com.vela.domain.usecase.ObserveAlertsByCoinIdUseCase
import com.vela.domain.usecase.ToggleWatchlistUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.pricepolling.PricePolling
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.base.pricepolling.PricePollingDelegate
import com.vela.ui.navigation.Screen
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailTab
import com.vela.ui.screens.detail.state.DetailUiState
import com.vela.ui.screens.detail.state.toCoinDetailUiModel
import com.vela.ui.screens.detail.state.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel
    @Inject
    constructor(
        private val getCoinDetail: GetCoinDetailUseCase,
        private val getOhlc: GetOhlcUseCase,
        private val assetPreviewCache: AssetPreviewCache,
        private val getPrices: GetPricesAndMarketDataUseCase,
        private val pricePolling: PricePollingController,
        private val isWatchlistedUseCase: IsWatchlistedUseCase,
        private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
        private val observeAlertsByCoinId: ObserveAlertsByCoinIdUseCase,
        savedStateHandle: SavedStateHandle
    ) : ViewModel(),
        PricePolling by pricePolling,
        PricePollingDelegate {
        val coinId: String
        val initialHeader: CoinDetailUiModel?

        private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
        val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

        private var ohlcJob: Job? = null
        var selectedRange by mutableStateOf(TimeRange.ONE_DAY)
            private set

        var isChartFullScreen by mutableStateOf(false)
            private set

        val isWatchlisted: StateFlow<Boolean>

        private val _alertFormEvent = MutableSharedFlow<Long?>(extraBufferCapacity = 1)
        val alertFormEvent = _alertFormEvent.asSharedFlow()

        var isAlertFormVisible by mutableStateOf(false)
            private set

        override fun getIds(): List<String> = listOf(coinId)

        override fun onPriceError(error: AppError?) {
            val current = _uiState.value as? DetailUiState.Success ?: return
            _uiState.value = current.copy(nonBlockingError = error)
        }

        override fun onCleared() {
            pricePolling.cancel()
            super.onCleared()
        }

        init {
            coinId =
                try {
                    savedStateHandle.toRoute<Screen.CoinDetail>().coinId
                } catch (_: Exception) {
                    savedStateHandle.get<String>("coinId") ?: ""
                }
            initialHeader = assetPreviewCache.get(coinId)?.toCoinDetailUiModel()

            isWatchlisted =
                isWatchlistedUseCase(coinId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                        initialValue = false
                    )

            pricePolling.bind(
                scope = viewModelScope,
                getPrices = getPrices,
                delegate = this
            )
            loadDetail()
        }

        fun toggleWatchlist() {
            viewModelScope.launch { toggleWatchlistUseCase(coinId) }
        }

        fun retry() = loadDetail(isRefresh = _uiState.value is DetailUiState.Success)

        fun onRangeSelected(range: TimeRange) {
            selectedRange = range
            loadOhlc(range)
        }

        fun toggleChartFullScreen() {
            isChartFullScreen = !isChartFullScreen
        }

        fun onTabSelected(tab: DetailTab) {
            val current = _uiState.value as? DetailUiState.Success ?: return
            _uiState.value = current.copy(selectedTab = tab)
        }

        fun openAlertEdit(id: Long? = null) {
            if (isChartFullScreen) return
            isAlertFormVisible = true
            _alertFormEvent.tryEmit(id)
        }

        fun dismissAlertEdit() {
            isAlertFormVisible = false
        }

        private fun observeAlerts() {
            observeAlertsByCoinId(coinId)
                .onEach { alerts ->
                    val current = _uiState.value as? DetailUiState.Success ?: return@onEach
                    _uiState.value = current.copy(alerts = alerts)
                }.launchIn(viewModelScope)
        }

        private fun loadDetail(isRefresh: Boolean = false) {
            if (isRefresh) {
                val current = _uiState.value as? DetailUiState.Success ?: return
                _uiState.value = current.copy(isRefreshing = true, nonBlockingError = null)
            } else {
                _uiState.value = DetailUiState.Loading
            }
            viewModelScope.launch {
                when (val result = getCoinDetail(coinId)) {
                    is DataResult.Success -> {
                        _uiState.value =
                            DetailUiState.Success(
                                detail = result.data.toUiModel(),
                                isChartLoading = true
                            )
                        loadOhlc(selectedRange)
                        observeAlerts()
                    }
                    is DataResult.Error -> {
                        val current = _uiState.value
                        if (current is DetailUiState.Success) {
                            _uiState.value =
                                current.copy(
                                    isRefreshing = false,
                                    nonBlockingError = result.appError
                                )
                        } else {
                            _uiState.value = DetailUiState.Error(result.appError)
                        }
                    }
                }
            }
        }

        private fun loadOhlc(range: TimeRange) {
            val current = _uiState.value as? DetailUiState.Success ?: return
            _uiState.value = current.copy(isChartLoading = true)
            ohlcJob?.cancel()
            ohlcJob =
                viewModelScope.launch {
                    when (val result = getOhlc(coinId, range)) {
                        is DataResult.Success -> {
                            val success = _uiState.value as? DetailUiState.Success ?: return@launch
                            _uiState.value =
                                success.copy(
                                    ohlcPoints = result.data,
                                    isChartLoading = false
                                )
                        }
                        is DataResult.Error -> {
                            val success = _uiState.value as? DetailUiState.Success ?: return@launch
                            _uiState.value =
                                success.copy(
                                    isChartLoading = false,
                                    nonBlockingError = result.appError
                                )
                        }
                    }
                }
        }
    }
