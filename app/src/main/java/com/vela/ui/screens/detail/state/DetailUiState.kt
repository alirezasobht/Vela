package com.vela.ui.screens.detail.state

import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.vela.domain.model.AppError

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val appError: AppError) : DetailUiState
    data class Success(
        val detail: CoinDetailUiModel,
        val candlestickModelPartial: CandlestickCartesianLayerModel.Partial? = null,
        val isChartLoading: Boolean = false,
        val nonBlockingError: AppError? = null
    ) : DetailUiState
}
