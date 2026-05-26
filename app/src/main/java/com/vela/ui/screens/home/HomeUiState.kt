package com.vela.ui.screens.home

import com.vela.domain.model.AppError
import com.vela.ui.common.components.model.AssetUiModel

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val assets: List<AssetUiModel>, val date: String) : HomeUiState()
    data class Error(val appError: AppError) : HomeUiState()
}
