package com.vela.ui.screens.search

import com.vela.domain.model.AppError
import com.vela.ui.common.components.model.AssetUiModel

sealed interface SearchUiState {
    data object Empty : SearchUiState

    data object Loading : SearchUiState

    data class Results(
        val assets: List<AssetUiModel>,
        val nonBlockingError: AppError? = null
    ) : SearchUiState

    data object NoResults : SearchUiState

    data class Error(val appError: AppError) : SearchUiState
}
