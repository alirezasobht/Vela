package com.vela.ui.screens.detail.state

import com.vela.domain.model.AppError

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val appError: AppError) : DetailUiState
    data class Success(
        val detail: CoinDetailUiModel,
        val nonBlockingError: AppError? = null
    ) : DetailUiState
}