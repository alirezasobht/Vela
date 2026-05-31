package com.vela.ui.screens.markets

import com.vela.domain.model.AppError

sealed interface MarketsUiState {
    data object Loading : MarketsUiState
    data class Error(val appError: AppError) : MarketsUiState
    data class Success(
        val isLoadingMore: Boolean = false,
        val nonBlockingError: AppError? = null
    ) : MarketsUiState
}