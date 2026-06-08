@file:OptIn(ExperimentalCoroutinesApi::class)

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
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.domain.usecase.GetMarketCategoriesUseCase
import com.vela.domain.usecase.GetMarketsUseCase
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.ui.base.AssetHolder
import com.vela.ui.base.pricepolling.PricePolling
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.base.pricepolling.PricePollingDelegate
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketsViewModel @Inject constructor(
    private val getMarkets: GetMarketsUseCase,
    private val getCategories: GetMarketCategoriesUseCase,
    private val assetHolder: AssetHolder,
    private val getPrices: GetPricesOnlyUseCase,
    private val pricePolling: PricePollingController
) : ViewModel(), PricePolling by pricePolling, PricePollingDelegate {

    private val _uiState = MutableStateFlow<MarketsUiState>(MarketsUiState.Loading)
    val uiState: StateFlow<MarketsUiState> = _uiState.asStateFlow()

    private var getCategoriesJob: Job? = null
    private val _categoriesLoaded = MutableStateFlow(false)
    private val _categories = MutableStateFlow<List<MarketCategory>>(listOf(MarketCategory.ALL))
    val categories: StateFlow<List<MarketCategory>> = _categories.asStateFlow()

    private val _loadedIds = MutableStateFlow<List<String>>(emptyList())

    var selectedCategory by mutableStateOf(MarketCategory.ALL)
        private set

    var selectedSort by mutableStateOf(MarketSort.MARKET_CAP)
        private set

    override fun getIds(): List<String> = _loadedIds.value

    override fun onPriceError(error: AppError?) {
        val current = _uiState.value as? MarketsUiState.Success ?: return
        _uiState.value = current.copy(nonBlockingError = error)
    }

    override fun onCleared() {
        pricePolling.cancel()
        super.onCleared()
    }

    val pagingFlow: Flow<PagingData<AssetUiModel>> = snapshotFlow {
        Pair(selectedCategory, selectedSort)
    }
        .flatMapLatest { (category, sort) ->
            getMarkets(category, sort).map { pagingData ->
                pagingData.map { asset ->
                    _loadedIds.update { current -> (current + asset.id).distinct() }
                    assetHolder.put(asset)
                    asset.toUiModel()
                }
            }
        }
        .cachedIn(viewModelScope)

    init {
        pricePolling.bind(
            scope = viewModelScope,
            getPrices = getPrices,
            delegate = this
        )
        fetchCategories()
    }

    fun retry() {
        getCategoriesJob?.cancel()
        if (!_categoriesLoaded.value) fetchCategories()
    }

    fun onCategorySelected(category: MarketCategory) {
        if (selectedCategory == category) return
        selectedCategory = category
        _loadedIds.value = emptyList()
    }

    fun onSortSelected(sort: MarketSort) {
        if (selectedSort == sort) return
        selectedSort = sort
        _loadedIds.value = emptyList()
    }

    fun onLoadStateChanged(loadState: CombinedLoadStates) {
        val refresh = loadState.refresh
        val append = loadState.append
        val currentSuccess = _uiState.value as? MarketsUiState.Success

        _uiState.value = when (refresh) {
            is LoadState.Loading -> MarketsUiState.Loading
            is LoadState.Error -> MarketsUiState.Error(refresh.error.toAppError())
            else -> MarketsUiState.Success(
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
                is DataResult.Error -> { /* silent — keep [ALL] */
                }
            }
        }
    }

    private fun loadCategories(categories: List<MarketCategory>) {
        _categoriesLoaded.value = true
        _categories.value = categories
    }
}
