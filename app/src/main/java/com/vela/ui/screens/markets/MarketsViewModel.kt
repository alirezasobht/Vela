@file:OptIn(ExperimentalCoroutinesApi::class)

package com.vela.ui.screens.markets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.domain.usecase.GetMarketCategoriesUseCase
import com.vela.domain.usecase.GetMarketsUseCase
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
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MarketsViewModel @Inject constructor(
    private val getMarkets: GetMarketsUseCase,
    private val getCategories: GetMarketCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MarketsUiState>(MarketsUiState.Loading)
    val uiState: StateFlow<MarketsUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<MarketCategory>>(listOf(MarketCategory.ALL))
    val categories: StateFlow<List<MarketCategory>> = _categories.asStateFlow()

    private val filterState = MutableStateFlow(Pair(MarketCategory.ALL, MarketSort.MARKET_CAP))

    var selectedCategory by mutableStateOf(MarketCategory.ALL)
        private set

    var selectedSort by mutableStateOf(MarketSort.MARKET_CAP)
        private set

    val pagingFlow: Flow<PagingData<AssetUiModel>> = filterState
        .flatMapLatest { (category, sort) ->
            getMarkets(category, sort).map { pagingData ->
                pagingData.map { it.toUiModel() }
            }
        }
        .cachedIn(viewModelScope)

    init {
        fetchCategories()
    }

    fun onCategorySelected(category: MarketCategory) {
        selectedCategory = category
        filterState.value = Pair(category, selectedSort)
    }

    fun onSortSelected(sort: MarketSort) {
        selectedSort = sort
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
                is DataResult.Error -> { /* silent — keep [ALL] */
                }
            }
        }
    }

    private fun Throwable.toAppError(): AppError = when (this) {
        is IOException -> AppError.NoInternet
        else -> AppError.Unknown(message ?: "Something went wrong")
    }
}