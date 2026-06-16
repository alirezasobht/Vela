package com.vela.ui.common.components.model

import androidx.compose.ui.graphics.Color

data class AssetUiModel(
    val id: String,
    val name: String,
    val image: String,
    val price: String,
    val priceChange: String,
    val symbolAndRank: String,
    val color: Color,
    val sparkline: List<Double>
)
