@file:OptIn(FlowPreview::class)

package com.vela.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.ObservePricesUseCase
import com.vela.domain.usecase.SearchAssetsUseCase
import com.vela.ui.base.PriceAwareViewModel
import com.vela.ui.common.components.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAssets: SearchAssetsUseCase,
    observePricesUseCase: ObservePricesUseCase
) : PriceAwareViewModel(observePricesUseCase) {

    var query by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchScope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())

    override val priceScope: CoroutineScope get() = searchScope
    override val assetIdsFlow: Flow<List<String>> = _uiState
        .map { state ->
            (state as? SearchUiState.Results)?.assets?.map { it.id } ?: emptyList()
        }

    init {
        viewModelScope.launch {
            snapshotFlow { query }
                .debounce(500)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isBlank()) resetSearch()
                    else startSearch(q)
                }
        }
        startPriceErrorObserver()
    }

    override fun onPriceError(error: AppError?) {
        val current = _uiState.value as? SearchUiState.Results ?: return
        _uiState.value = current.copy(nonBlockingError = error)
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        if (newQuery.isBlank()) resetSearch()
    }

    fun retry() {
        if (query.isNotBlank()) startSearch(query)
    }

    private fun resetSearch() {
        searchScope.coroutineContext.cancelChildren()
        _uiState.value = SearchUiState.Empty
    }

    private fun startSearch(q: String) {
        searchScope.coroutineContext.cancelChildren()
        _uiState.value = SearchUiState.Loading
        searchScope.launch {
            when (val result = searchAssets(q)) {
                is DataResult.Success -> {
                    val assets = result.data
                    if (assets.isEmpty()) {
                        _uiState.value = SearchUiState.NoResults
                    } else {
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

    override fun onCleared() {
        super.onCleared()
        searchScope.cancel()
    }
}