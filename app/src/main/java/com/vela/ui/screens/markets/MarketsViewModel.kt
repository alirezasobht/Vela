@file:OptIn(ExperimentalCoroutinesApi::class)

package com.vela.ui.screens.markets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
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
import com.vela.domain.usecase.GetPricesUseCase
import com.vela.ui.base.PriceAwareViewModel
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    getPrices: GetPricesUseCase,
    savedStateHandle: SavedStateHandle
) : PriceAwareViewModel(getPrices, savedStateHandle) {

    private val _uiState = MutableStateFlow<MarketsUiState>(MarketsUiState.Loading)
    val uiState: StateFlow<MarketsUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<MarketCategory>>(listOf(MarketCategory.ALL))
    val categories: StateFlow<List<MarketCategory>> = _categories.asStateFlow()

    private val filterState = MutableStateFlow(Pair(MarketCategory.ALL, MarketSort.MARKET_CAP))
    private val _loadedIds = MutableStateFlow<List<String>>(emptyList())

    var selectedCategory by mutableStateOf(MarketCategory.ALL)
        private set

    var selectedSort by mutableStateOf(MarketSort.MARKET_CAP)
        private set

    override fun getIdsForPricing(): List<String> = _loadedIds.value

    val pagingFlow: Flow<PagingData<AssetUiModel>> = filterState
        .flatMapLatest { (category, sort) ->
            getMarkets(category, sort).map { pagingData ->
                pagingData.map { asset ->
                    _loadedIds.update { current -> (current + asset.id).distinct() }
                    asset.toUiModel()
                }
            }
        }
        .cachedIn(viewModelScope)

    init {
        fetchCategories()
    }

    override fun onPriceError(error: AppError?) {
        val current = _uiState.value as? MarketsUiState.Success ?: return
        _uiState.value = current.copy(nonBlockingError = error)
    }

    fun onCategorySelected(category: MarketCategory) {
        selectedCategory = category
        _loadedIds.value = emptyList()
        filterState.value = Pair(category, selectedSort)
    }

    fun onSortSelected(sort: MarketSort) {
        selectedSort = sort
        _loadedIds.value = emptyList()
        filterState.value = Pair(selectedCategory, sort)
    }

    fun onLoadStateChanged(loadState: CombinedLoadStates) {
        val current = _uiState.value
        val refresh = loadState.refresh
        val append = loadState.append

        _uiState.value = when {
            refresh is LoadState.Loading -> MarketsUiState.Loading
            refresh is LoadState.Error -> MarketsUiState.Error(refresh.error.toAppError())
            append is LoadState.Loading -> MarketsUiState.Success(
                isLoadingMore = true,
                nonBlockingError = (current as? MarketsUiState.Success)?.nonBlockingError
            )
            append is LoadState.Error -> MarketsUiState.Success(
                isLoadingMore = false,
                nonBlockingError = append.error.toAppError()
            )
            else -> MarketsUiState.Success(
                isLoadingMore = false,
                nonBlockingError = (current as? MarketsUiState.Success)?.nonBlockingError
            )
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            when (val result = getCategories()) {
                is DataResult.Success -> _categories.value = result.data
                is DataResult.Error -> { /* silent — keep [ALL] */ }
            }
        }
    }
}
