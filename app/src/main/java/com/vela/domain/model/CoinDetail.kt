package com.vela.domain.model

data class CoinDetail(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String?,
    val currentPrice: Double?,
    val priceChange24h: Double?,
    val priceChangePercent24h: Double?,
    val marketCap: Double?,
    val totalVolume: Double?,
    val circulatingSupply: Double?,
    val ath: Double?,
    val atl: Double?,
    val marketCapRank: Int?
)