package com.vela.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetWatchlistAssetsUseCase
import com.vela.domain.usecase.ToggleWatchlistUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.pricepolling.PricePolling
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.base.pricepolling.PricePollingDelegate
import com.vela.ui.common.components.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlistAssets: GetWatchlistAssetsUseCase,
    private val toggleWatchlist: ToggleWatchlistUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val pricePolling: PricePollingController
) : ViewModel(), PricePolling by pricePolling, PricePollingDelegate {

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    override fun getIds(): List<String> =
        (_uiState.value as? WatchlistUiState.Success)?.assets?.map { it.id } ?: emptyList()

    override fun onPriceError(error: AppError?) { /* non-blocking, watchlist stays visible */ }

    override fun onCleared() {
        pricePolling.cancel()
        super.onCleared()
    }

    init {
        pricePolling.bind(
            scope = viewModelScope,
            getPrices = TODO("inject GetPricesOnlyUseCase"),
            delegate = this
        )
        observeWatchlist()
    }

    fun removeFromWatchlist(coinId: String) {
        viewModelScope.launch { toggleWatchlist(coinId) }
    }

    private fun observeWatchlist() {
        getWatchlistAssets()
            .onEach { result ->
                _uiState.value = when (result) {
                    is DataResult.Success -> {
                        val assets = result.data
                        assets.forEach { assetPreviewCache.put(it) }
                        if (assets.isEmpty()) WatchlistUiState.Empty
                        else WatchlistUiState.Success(assets.map { it.toUiModel() })
                    }
                    is DataResult.Error -> WatchlistUiState.Empty
                }
            }
            .launchIn(viewModelScope)
    }
}