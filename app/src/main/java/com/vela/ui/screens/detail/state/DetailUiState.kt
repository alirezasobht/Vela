package com.vela.ui.screens.detail.state

import com.vela.domain.model.AppError
import com.vela.domain.model.OhlcPoint

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val appError: AppError) : DetailUiState
    data class Success(
        val detail: CoinDetailUiModel,
        val ohlcPoints: List<OhlcPoint> = emptyList(),
        val isChartLoading: Boolean = false,
        val nonBlockingError: AppError? = null
    ) : DetailUiState
}
