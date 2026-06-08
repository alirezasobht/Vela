@file:OptIn(ExperimentalCoroutinesApi::class)

package com.vela.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.domain.usecase.GetTodayUseCase
import com.vela.domain.usecase.GetTopAssetsUseCase
import com.vela.domain.usecase.RefreshAssetsUseCase
import com.vela.ui.base.AssetHolder
import com.vela.ui.base.pricepolling.PricePolling
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.base.pricepolling.PricePollingDelegate
import com.vela.ui.common.components.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopAssets: GetTopAssetsUseCase,
    private val refreshAssets: RefreshAssetsUseCase,
    private val getToday: GetTodayUseCase,
    private val assetHolder: AssetHolder,
    private val getPrices: GetPricesOnlyUseCase,
    private val pricePolling: PricePollingController
) : ViewModel(), PricePolling by pricePolling, PricePollingDelegate {

    private val _pullRefreshing = MutableStateFlow(false)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    private val refreshMutex = Mutex()

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val pullRefreshing: StateFlow<Boolean> = _pullRefreshing.asStateFlow()

    var selectedLimit: Int by mutableIntStateOf(50)
        private set

    override fun getIds(): List<String> =
        (_uiState.value as? HomeUiState.Success)?.assets?.map { it.id } ?: emptyList()

    override fun onPriceError(error: AppError?) {
        val current = _uiState.value as? HomeUiState.Success ?: return
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
        startFresh(selectedLimit)
    }

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
            // wait for first refresh
            val success = doRefresh(limit)
            // if success start observing and refresh loop
            if (success) {
                launch { observeAssets(limit) }
            }
        }
    }

    private suspend fun doRefresh(limit: Int, isPullRefresh: Boolean = false): Boolean {
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
                        if (current is HomeUiState.Success) _uiState.value = current.copy(nonBlockingError = result.appError)
                        else _uiState.value = HomeUiState.Error(result.appError)
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
                    assetHolder.put(result.data)
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

    private fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
}