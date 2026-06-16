package com.vela.data.source.remote.model

import com.squareup.moshi.Json

data class CoinDetailDto(
    @Json(name = "id") val id: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String?,
    @Json(name = "current_price") val currentPrice: Double?,
    @Json(name = "price_change_24h") val priceChange24h: Double?,
    @Json(name = "price_change_percentage_24h") val priceChangePercent24h: Double?,
    @Json(name = "market_cap") val marketCap: Double?,
    @Json(name = "total_volume") val totalVolume: Double?,
    @Json(name = "circulating_supply") val circulatingSupply: Double?,
    @Json(name = "ath") val ath: Double?,
    @Json(name = "atl") val atl: Double?,
    @Json(name = "market_cap_rank") val marketCapRank: Int?
)
