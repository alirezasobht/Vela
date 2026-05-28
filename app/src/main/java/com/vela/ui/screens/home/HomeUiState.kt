package com.vela.ui.screens.home

import com.vela.domain.model.AppError
import com.vela.ui.common.components.model.AssetUiModel

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val assets: List<AssetUiModel>,
        val formattedDate: String,
        val nonBlockingError: AppError? = null
    ) : HomeUiState

    data class Error(
        val appError: AppError
    ) : HomeUiState
}
