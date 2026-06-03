package com.vela.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetCoinDetailUseCase
import com.vela.domain.usecase.GetPricesAndMarketDataUseCase
import com.vela.ui.base.PriceAwareViewModel
import com.vela.ui.navigation.DetailDestination
import com.vela.ui.screens.detail.state.DetailUiState
import com.vela.ui.screens.detail.state.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCoinDetail: GetCoinDetailUseCase,
    getFullPrices: GetPricesAndMarketDataUseCase,
    savedStateHandle: SavedStateHandle
) : PriceAwareViewModel(getFullPrices, savedStateHandle) {

    private val coinId: String = checkNotNull(savedStateHandle[DetailDestination.ARG_COIN_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    override fun getIdsForPricing(): List<String> = listOf(coinId)

    override fun onPriceError(error: AppError?) {
        val current = _uiState.value as? DetailUiState.Success ?: return
        _uiState.value = current.copy(nonBlockingError = error)
    }

    init {
        loadDetail()
    }

    fun retry() = loadDetail()

    private fun loadDetail() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            when (val result = getCoinDetail(coinId)) {
                is DataResult.Success -> _uiState.value = DetailUiState.Success(result.data.toUiModel())
                is DataResult.Error -> _uiState.value = DetailUiState.Error(result.appError)
            }
        }
    }
}