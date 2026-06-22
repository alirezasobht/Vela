package com.vela.ui.screens.alerts

sealed interface AlertsUiState {
    data object Loading : AlertsUiState
    data object Empty : AlertsUiState
    data class Success(val groups: List<AlertGroupUiModel>) : AlertsUiState
}
