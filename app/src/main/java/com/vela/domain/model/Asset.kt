package com.vela.domain.model

data class Asset(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String?,
    val currentPrice: Double?,
    val priceChangePercent24h: Double?,
    val marketCapRank: Int?,
    val sparkline: List<Double>?
)