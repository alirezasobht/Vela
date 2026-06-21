package com.vela.data.source.remote.model

import com.squareup.moshi.Json

data class CoinDetailDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "symbol") val symbol: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "image") val image: String?,
    @param:Json(name = "current_price") val currentPrice: Double?,
    @param:Json(name = "price_change_24h") val priceChange24h: Double?,
    @param:Json(name = "price_change_percentage_24h") val priceChangePercent24h: Double?,
    @param:Json(name = "market_cap") val marketCap: Double?,
    @param:Json(name = "total_volume") val totalVolume: Double?,
    @param:Json(name = "circulating_supply") val circulatingSupply: Double?,
    @param:Json(name = "ath") val ath: Double?,
    @param:Json(name = "atl") val atl: Double?,
    @param:Json(name = "market_cap_rank") val marketCapRank: Int?
)
