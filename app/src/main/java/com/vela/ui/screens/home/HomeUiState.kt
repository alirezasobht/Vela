package com.vela.ui.screens.home

import com.vela.ui.common.components.model.AssetUiModel

data class HomeUiState(
    val assets: List<AssetUiModel> = emptyList(),
    val date: String = ""
)
