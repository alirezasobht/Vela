package com.vela.ui.screens.detail.state

data class CoinDetailUiModel(
    val id: String,
    val name: String,
    val symbol: String,
    val image: String?,
    val currentPrice: String,
    val priceChange24h: String,
    val priceChangePercent24h: String,
    val isPositive: Boolean,
    val marketCap: String,
    val totalVolume: String,
    val circulatingSupply: String,
    val ath: String,
    val atl: String
)