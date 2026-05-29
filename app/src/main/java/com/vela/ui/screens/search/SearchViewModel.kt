@file:OptIn(FlowPreview::class)

package com.vela.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetSearchPricesUseCase
import com.vela.domain.usecase.SearchAssetsUseCase
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
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
    private val getSearchPrices: GetSearchPricesUseCase
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val pricesLoadedOnce = MutableStateFlow(false)
    private val searchScope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())

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
        pricesLoadedOnce.value = false
        _uiState.value = SearchUiState.Empty
    }

    private fun startSearch(q: String) {
        searchScope.coroutineContext.cancelChildren()
        pricesLoadedOnce.value = false
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
                        launch { startPricePolling(assets) }
                    }
                }

                is DataResult.Error -> {
                    _uiState.value = SearchUiState.Error(result.appError)
                }
            }
        }
    }

    private suspend fun startPricePolling(assets: List<Asset>) {
        val ids = assets.map { it.id }
        while (true) {
            fetchPrices(ids)
            delay(5_000)
        }
    }

    private suspend fun fetchPrices(ids: List<String>) {
        val current = _uiState.value as? SearchUiState.Results ?: return
        when (val result = getSearchPrices(ids)) {
            is DataResult.Success -> {
                pricesLoadedOnce.value = true
                _uiState.value = current.copy(
                    assets = mergeWithPrices(current.assets, result.data),
                    nonBlockingError = null
                )
            }

            is DataResult.Error -> {
                if (pricesLoadedOnce.value) {
                    _uiState.value = current.copy(nonBlockingError = result.appError)
                }
            }
        }
    }

    private fun mergeWithPrices(
        current: List<AssetUiModel>,
        priceAssets: List<Asset>
    ): List<AssetUiModel> {
        val priceMap = priceAssets.associateBy { it.id }
        return current.map { uiModel ->
            priceMap[uiModel.id]?.toUiModel() ?: uiModel
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchScope.cancel()
    }
}