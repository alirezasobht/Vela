@file:OptIn(FlowPreview::class)

package com.vela.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.DataResult
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.usecase.SearchAssetsUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.watchlist.WatchlistController
import com.vela.ui.base.watchlist.WatchlistControllerImpl
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.components.model.SimplePriceUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAssets: SearchAssetsUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val priceStore: SimplePriceStore,
    private val pricePolling: PricePolling,
    private val watchlistController: WatchlistControllerImpl
) : ViewModel(),
    WatchlistController by watchlistController {
    var query by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onScreenVisible(visible: Boolean) {
        pricePolling.onScreenVisible(visible)
    }

    fun observePrice(id: String): Flow<SimplePriceUiModel?> = priceStore.observePrice(id).map { it?.toUiModel() }

    init {
        watchlistController.bind(viewModelScope)

        viewModelScope.launch {
            pricePolling.observeError().collect { error ->
                val current = _uiState.value as? SearchUiState.Results ?: return@collect
                _uiState.value = current.copy(nonBlockingError = error)
            }
        }

        viewModelScope.launch {
            snapshotFlow { query }
                .debounce(500)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isBlank()) {
                        resetSearch()
                    } else {
                        startSearch(q)
                    }
                }
        }
    }

    fun assetListItemActions(onClick: (String) -> Unit): AssetListItemActions = AssetListItemActions(
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
                    if (assets.isEmpty()) {
                        _uiState.value = SearchUiState.NoResults
                    } else {
                        pricePolling.register(assets.map { it.id }.toSet())
                        _uiState.value = SearchUiState.Results(
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
