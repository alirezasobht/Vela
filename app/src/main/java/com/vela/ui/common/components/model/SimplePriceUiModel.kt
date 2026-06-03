package com.vela.ui.common.components.model

data class SimplePriceUiModel(
    val price: String,
    val priceChange: String,
    val isPositive: Boolean,
    val marketCap: String? = null,
    val totalVolume: String? = null
)