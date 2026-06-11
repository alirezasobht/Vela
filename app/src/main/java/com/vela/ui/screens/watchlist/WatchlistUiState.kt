package com.vela.ui.screens.watchlist

import com.vela.ui.common.components.model.AssetUiModel

sealed interface WatchlistUiState {
    data object Loading : WatchlistUiState
    data object Empty : WatchlistUiState
    data class Success(val assets: List<AssetUiModel>) : WatchlistUiState
}