@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.vela.ui.screens.markets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.vela.data.source.remote.util.toAppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.usecase.GetMarketCategoriesUseCase
import com.vela.domain.usecase.GetMarketsUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.watchlist.WatchlistController
import com.vela.ui.base.watchlist.WatchlistControllerImpl
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MarketsViewModel @Inject constructor(
    private val getMarkets: GetMarketsUseCase,
    private val getCategories: GetMarketCategoriesUseCase,
    private val assetPreviewCache: AssetPreviewCache,
    private val priceStore: SimplePriceStore,
    private val pricePolling: PricePolling,
    private val watchlistController: WatchlistControllerImpl
) : ViewModel(),
    WatchlistController by watchlistController {
    private val _uiState = MutableStateFlow<MarketsUiState>(MarketsUiState.Loading)
    val uiState: StateFlow<MarketsUiState> = _uiState.asStateFlow()

    private var getCategoriesJob: Job? = null
    private val categoriesLoaded = MutableStateFlow(false)
    private val _categories = MutableStateFlow<List<MarketCategory>>(listOf(MarketCategory.ALL))
    val categories: StateFlow<List<MarketCategory>> = _categories.asStateFlow()

    private val pendingRegisterIds = MutableStateFlow<Set<String>>(emptySet())

    var selectedCategory by mutableStateOf(MarketCategory.ALL)
        private set

    var selectedSort by mutableStateOf(MarketSort.MARKET_CAP)
        private set

    val pagingFlow: Flow<PagingData<AssetUiModel>> = snapshotFlow {
        Pair(selectedCategory, selectedSort)
    }.flatMapLatest { (category, sort) ->
        getMarkets(category, sort).map { pagingData ->
            pagingData.map { asset ->
                assetPreviewCache.put(asset)
                pendingRegisterIds.update { it + asset.id }
                asset.toUiModel()
            }
        }
    }.cachedIn(viewModelScope)

    init {
        watchlistController.bind(viewModelScope)
        fetchCategories()
        viewModelScope.launch {
            pricePolling.observeError().collect { error ->
                val current = _uiState.value as? MarketsUiState.Success ?: return@collect
                _uiState.value = current.copy(nonBlockingError = error)
            }
        }
        viewModelScope.launch {
            pendingRegisterIds
                .debounce(200.milliseconds)
                .collect { ids ->
                    if (ids.isNotEmpty()) {
                        pricePolling.register(ids)
                        pendingRegisterIds.value = emptySet()
                    }
                }
        }
    }

    fun onScreenVisible(visible: Boolean) {
        pricePolling.onScreenVisible(visible)
    }

    fun observePrice(id: String): Flow<SimplePriceUiModel?> = priceStore.observePrice(id).map { it?.toUiModel() }

    fun assetListItemActions(onClick: (String) -> Unit): AssetListItemActions = AssetListItemActions(
        observePrice = ::observePrice,
        observeIsWatchlisted = ::observeIsWatchlisted,
        onToggleWatchlist = ::toggleWatchlist,
        onClick = onClick
    )

    fun retry() {
        getCategoriesJob?.cancel()
        if (!categoriesLoaded.value) fetchCategories()
    }

    fun onCategorySelected(category: MarketCategory) {
        if (selectedCategory == category) return
        selectedCategory = category
    }

    fun onSortSelected(sort: MarketSort) {
        if (selectedSort == sort) return
        selectedSort = sort
    }

    fun onLoadStateChanged(loadState: CombinedLoadStates) {
        val refresh = loadState.refresh
        val append = loadState.append
        val currentSuccess = _uiState.value as? MarketsUiState.Success

        _uiState.value = when (refresh) {
            is LoadState.Loading -> MarketsUiState.Loading

            is LoadState.Error -> MarketsUiState.Error(refresh.error.toAppError())

            else ->
                MarketsUiState.Success(
                    isLoadingMore = append is LoadState.Loading,
                    nonBlockingError = (append as? LoadState.Error)?.error?.toAppError()
                        ?: currentSuccess?.nonBlockingError
                )
        }
    }

    private fun fetchCategories() {
        getCategoriesJob = viewModelScope.launch {
            when (val result = getCategories()) {
                is DataResult.Success -> loadCategories(result.data)

                is DataResult.Error -> {
                }
            }
        }
    }

    private fun loadCategories(categories: List<MarketCategory>) {
        categoriesLoaded.value = true
        _categories.value = categories
    }
}
