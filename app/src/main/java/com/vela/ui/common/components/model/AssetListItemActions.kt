package com.vela.ui.common.components.model

import kotlinx.coroutines.flow.Flow

data class AssetListItemActions(
    val observePrice: (String) -> Flow<SimplePriceUiModel?>,
    val observeIsWatchlisted: (String) -> Flow<Boolean>,
    val onToggleWatchlist: (String) -> Unit,
    val onClick: (String) -> Unit
)
