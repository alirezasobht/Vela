package com.vela.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.DataResult
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.usecase.GetTodayUseCase
import com.vela.domain.usecase.GetTopAssetsUseCase
import com.vela.domain.usecase.RefreshAssetsUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.watchlist.WatchlistController
import com.vela.ui.base.watchlist.WatchlistControllerImpl
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.components.model.SimplePriceUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopAssets: GetTopAssetsUseCase,
    private val refreshAssets: RefreshAssetsUseCase,
    private val getToday: GetTodayUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val priceStore: SimplePriceStore,
    private val pricePolling: PricePolling,
    private val watchlistController: WatchlistControllerImpl
) : ViewModel(),
    WatchlistController by watchlistController {
    private val _pullRefreshing = MutableStateFlow(false)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    private val refreshMutex = Mutex()
    private val errorScope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val pullRefreshing: StateFlow<Boolean> = _pullRefreshing.asStateFlow()

    var selectedLimit: Int by mutableIntStateOf(50)
        private set

    fun onScreenVisible(visible: Boolean) {
        pricePolling.onScreenVisible(visible)
    }

    fun observePrice(id: String): Flow<SimplePriceUiModel?> = priceStore.observePrice(id).map { it?.toUiModel() }

    override fun onCleared() {
        errorScope.cancel()
        super.onCleared()
    }

    init {
        watchlistController.bind(viewModelScope)
        errorScope.launch {
            pricePolling.observeError().collect { error ->
                val current = _uiState.value as? HomeUiState.Success ?: return@collect
                _uiState.value = current.copy(nonBlockingError = error)
            }
        }
        startFresh(selectedLimit)
    }

    fun assetListItemActions(onClick: (String) -> Unit): AssetListItemActions = AssetListItemActions(
        observePrice = ::observePrice,
        observeIsWatchlisted = ::observeIsWatchlisted,
        onToggleWatchlist = ::toggleWatchlist,
        onClick = onClick
    )

    fun onLimitChanged(limit: Int) {
        selectedLimit = limit
        startFresh(limit)
    }

    fun retry() = startFresh(selectedLimit)

    fun pullToRefresh() {
        _pullRefreshing.value = true
        viewModelScope.launch { doRefresh(selectedLimit, isPullRefresh = true) }
    }

    private fun startFresh(limit: Int) {
        viewModelScope.coroutineContext.cancelChildren()
        _pullRefreshing.value = false
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            val success = doRefresh(limit)
            if (success) {
                launch { observeAssets(limit) }
            }
        }
    }

    private suspend fun doRefresh(
        limit: Int,
        isPullRefresh: Boolean = false
    ): Boolean {
        if (refreshMutex.isLocked) {
            if (isPullRefresh) _pullRefreshing.value = false
            return false
        }
        var success = false
        runCatching {
            refreshMutex.withLock {
                if (_uiState.value !is HomeUiState.Success) _uiState.value = HomeUiState.Loading
                when (val result = refreshAssets(limit)) {
                    is DataResult.Success -> {
                        success = true
                        val current = _uiState.value
                        if (current is HomeUiState.Success) _uiState.value = current.copy(nonBlockingError = null)
                    }

                    is DataResult.Error -> {
                        val current = _uiState.value
                        if (current is HomeUiState.Success) {
                            _uiState.value = current.copy(nonBlockingError = result.appError)
                        } else {
                            _uiState.value = HomeUiState.Error(result.appError)
                        }
                    }
                }
            }
        }
        if (isPullRefresh) _pullRefreshing.value = false
        return success
    }

    private suspend fun observeAssets(limit: Int) {
        getTopAssets(limit).collect { result ->
            when (result) {
                is DataResult.Success -> {
                    assetPreviewCache.put(result.data)
                    if (result.data.isNotEmpty()) {
                        pricePolling.register(result.data.map { it.id }.toSet())
                    }
                    val current = _uiState.value
                    _uiState.value = HomeUiState.Success(
                        assets = result.data.map { it.toUiModel() },
                        formattedDate = formatDate(getToday()),
                        nonBlockingError = (current as? HomeUiState.Success)?.nonBlockingError
                    )
                }

                is DataResult.Error -> {
                    if (_uiState.value !is HomeUiState.Success) {
                        _uiState.value = HomeUiState.Error(result.appError)
                    }
                }
            }
        }
    }

    private fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
}
