package com.vela.ui.screens.detail

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
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.navigation.Screen
import com.vela.ui.screens.alerts.AlertLabelFormatter
import com.vela.ui.screens.alerts.AlertRowUiModel
import com.vela.ui.screens.detail.state.AlertFormState
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailAction
import com.vela.ui.screens.detail.state.DetailContentState
import com.vela.ui.screens.detail.state.DetailScreenState
import com.vela.ui.screens.detail.state.DetailTab
import com.vela.ui.screens.detail.state.toCoinDetailUiModel
import com.vela.ui.screens.detail.state.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCoinDetail: GetCoinDetailUseCase,
    private val getOhlc: GetOhlcUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val getPrices: GetPricesAndMarketDataUseCase,
    private val pricePolling: PricePollingController,
    private val isWatchlistedUseCase: IsWatchlistedUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val observeAlertsByCoinId: ObserveAlertsByCoinIdUseCase,
    private val alertLabelFormatter: AlertLabelFormatter,
    savedStateHandle: SavedStateHandle
) : ViewModel(),
    PricePolling by pricePolling,
    PricePollingDelegate {

    val initialHeader: CoinDetailUiModel?

    private val initialTab: DetailTab
    private val initialAlertId: Long?

    private val _state = MutableStateFlow(DetailScreenState())
    val state: StateFlow<DetailScreenState> = _state.asStateFlow()

    private var ohlcJob: Job? = null

    override fun getIds(): List<String> = listOf(_state.value.coinId)

    override fun onPriceError(error: AppError?) {
        updateContent<DetailContentState.Success> { copy(nonBlockingError = error) }
    }

    override fun onCleared() {
        pricePolling.cancel()
        super.onCleared()
    }

    init {
        val route = try {
            savedStateHandle.toRoute<Screen.CoinDetail>()
        } catch (_: Exception) {
            null
        }
        val coinId = route?.coinId ?: savedStateHandle.get<String>("coinId") ?: ""
        initialTab = route?.initialTab ?: DetailTab.STATS
        initialAlertId = route?.initialAlertId

        initialHeader = assetPreviewCache.get(coinId)?.toCoinDetailUiModel()

        _state.update { it.copy(coinId = coinId) }

        isWatchlistedUseCase(coinId)
            .onEach { isWatchlisted -> _state.update { it.copy(isWatchlisted = isWatchlisted) } }
            .launchIn(viewModelScope)

        pricePolling.bind(
            scope = viewModelScope,
            getPrices = getPrices,
            delegate = this
        )

        viewModelScope.launch {
            observePrice(coinId).collect { livePrice ->
                if (livePrice != null) applyLivePrice(livePrice)
            }
        }

        loadDetail()
    }

    fun onAction(action: DetailAction) {
        when (action) {
            DetailAction.Back -> Unit
            DetailAction.Retry -> retry()
            DetailAction.ToggleFullScreen -> toggleChartFullScreen()
            DetailAction.ToggleWatchlist -> toggleWatchlist()
            DetailAction.DismissAlertForm -> dismissAlertForm()
            is DetailAction.RangeSelected -> onRangeSelected(action.range)
            is DetailAction.TabSelected -> onTabSelected(action.tab)
            is DetailAction.EditAlert -> openAlertForm(action.id)
        }
    }

    private fun applyLivePrice(livePrice: SimplePriceUiModel) {
        updateContent<DetailContentState.Success> {
            copy(
                detail = detail.copy(
                    currentPrice = livePrice.price,
                    priceChangePercent24h = livePrice.priceChange,
                    priceChange24h = livePrice.priceChange24h ?: detail.priceChange24h,
                    isPositive = livePrice.isPositive,
                    marketCap = livePrice.marketCap ?: detail.marketCap,
                    totalVolume = livePrice.totalVolume ?: detail.totalVolume
                )
            )
        }
    }

    private fun toggleWatchlist() {
        viewModelScope.launch { toggleWatchlistUseCase(_state.value.coinId) }
    }

    private fun retry() = loadDetail(isRefresh = _state.value.content is DetailContentState.Success)

    private fun onRangeSelected(range: TimeRange) {
        _state.update { it.copy(selectedRange = range) }
        loadOhlc(range)
    }

    private fun toggleChartFullScreen() {
        _state.update { it.copy(isChartFullScreen = !it.isChartFullScreen) }
    }

    private fun onTabSelected(tab: DetailTab) {
        updateContent<DetailContentState.Success> { copy(selectedTab = tab) }
    }

    private fun openAlertForm(id: Long?) {
        val alerts = (_state.value.content as? DetailContentState.Success)?.alerts ?: emptyList()
        val alertRow = alerts.find { it.id == id }
        val currentSessionId = (_state.value.alertFormState as? AlertFormState.Visible)?.formSessionId ?: 0
        _state.update {
            it.copy(
                alertFormState = AlertFormState.Visible(
                    formSessionId = currentSessionId + 1,
                    alertRowUiModel = alertRow
                )
            )
        }
    }

    private fun dismissAlertForm() {
        _state.update { it.copy(alertFormState = AlertFormState.Invisible) }
    }

    private fun observeAlerts() {
        observeAlertsByCoinId(_state.value.coinId)
            .onEach { alerts ->
                val rows = alerts.map { alert ->
                    AlertRowUiModel(
                        id = alert.id,
                        label = alertLabelFormatter.format(alert),
                        isTriggered = alert.isTriggered
                    )
                }
                updateContent<DetailContentState.Success> { copy(alerts = rows) }
            }.launchIn(viewModelScope)
    }

    private fun loadDetail(isRefresh: Boolean = false) {
        if (isRefresh) {
            updateContent<DetailContentState.Success> { copy(isRefreshing = true, nonBlockingError = null) }
        } else {
            _state.update { it.copy(content = DetailContentState.Loading) }
        }
        viewModelScope.launch {
            when (val result = getCoinDetail(_state.value.coinId)) {
                is DataResult.Success -> {
                    _state.update {
                        it.copy(
                            content = DetailContentState.Success(
                                detail = result.data.toUiModel(),
                                isChartLoading = true,
                                selectedTab = initialTab
                            )
                        )
                    }
                    loadOhlc(_state.value.selectedRange)
                    observeAlerts()
                    if (initialAlertId != null) openAlertForm(initialAlertId)
                }

                is DataResult.Error -> {
                    val current = _state.value.content
                    if (current is DetailContentState.Success) {
                        updateContent<DetailContentState.Success> {
                            copy(isRefreshing = false, nonBlockingError = result.appError)
                        }
                    } else {
                        _state.update { it.copy(content = DetailContentState.Error(result.appError)) }
                    }
                }
            }
        }
    }

    private fun loadOhlc(range: TimeRange) {
        updateContent<DetailContentState.Success> { copy(isChartLoading = true) }
        ohlcJob?.cancel()
        ohlcJob = viewModelScope.launch {
            when (val result = getOhlc(_state.value.coinId, range)) {
                is DataResult.Success -> {
                    updateContent<DetailContentState.Success> {
                        copy(ohlcPoints = result.data, isChartLoading = false)
                    }
                }

                is DataResult.Error -> {
                    updateContent<DetailContentState.Success> {
                        copy(isChartLoading = false, nonBlockingError = result.appError)
                    }
                }
            }
        }
    }

    private inline fun <reified T : DetailContentState> updateContent(crossinline block: T.() -> DetailContentState) {
        _state.update { screen ->
            val current = screen.content as? T ?: return@update screen
            screen.copy(content = current.block())
        }
    }
}
