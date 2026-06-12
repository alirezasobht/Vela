@file:OptIn(FlowPreview::class)

package com.vela.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.domain.usecase.SearchAssetsUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.pricepolling.PricePolling
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.base.pricepolling.PricePollingDelegate
import com.vela.ui.base.watchlist.WatchlistController
import com.vela.ui.base.watchlist.WatchlistControllerImpl
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAssets: SearchAssetsUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val getPrices: GetPricesOnlyUseCase,
    private val pricePolling: PricePollingController,
    private val watchlistController: WatchlistControllerImpl
) : ViewModel(), PricePolling by pricePolling, PricePollingDelegate, WatchlistController by watchlistController {

    var query by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    override fun getIds(): List<String> =
        (_uiState.value as? SearchUiState.Results)?.assets?.map { it.id } ?: emptyList()

    override fun onPriceError(error: AppError?) {
        val current = _uiState.value as? SearchUiState.Results ?: return
        _uiState.value = current.copy(nonBlockingError = error)
    }

    override fun onCleared() {
        pricePolling.cancel()
        super.onCleared()
    }

    init {
        pricePolling.bind(
            scope = viewModelScope,
            getPrices = getPrices,
            delegate = this
        )
        watchlistController.bind(viewModelScope)

        viewModelScope.launch {
            snapshotFlow { query }
                .debounce(500)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isBlank()) resetSearch()
                    else startSearch(q)
                }
        }
    }

    fun assetListItemActions(onClick: (String) -> Unit): AssetListItemActions =
        AssetListItemActions(
            observePrice = ::observePrice,
            observeIsWatchlisted = ::observeIsWatchlisted,
            onToggleWatchlist = ::toggleWatchlist,
            onClick = onClick
        )

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    fun onClearQuery() {
        resetSearch()
        onQueryChange("")
    }

    fun retry() {
        if (query.isNotBlank()) startSearch(query)
    }

    private fun resetSearch() {
        searchJob?.cancel()
        _uiState.value = SearchUiState.Empty
    }

    private fun startSearch(q: String) {
        searchJob?.cancel()
        _uiState.value = SearchUiState.Loading
        searchJob = viewModelScope.launch {
            when (val result = searchAssets(q)) {
                is DataResult.Success -> {
                    val assets = result.data
                    assetPreviewCache.put(assets)
                    _uiState.value = if (assets.isEmpty()) {
                        SearchUiState.NoResults
                    } else {
                        SearchUiState.Results(
                            assets = assets.map { it.toUiModel() }
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.value = SearchUiState.Error(result.appError)
                }
            }
        }
    }
}