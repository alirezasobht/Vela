package com.vela.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoinDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "symbol") val symbol: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "image") val image: String?,
    @param:Json(name = "current_price") val currentPrice: Double?,
    @param:Json(name = "price_change_percentage_24h") val priceChangePercent24h: Double?,
    @param:Json(name = "market_cap_rank") val marketCapRank: Int?,
    @param:Json(name = "sparkline_in_7d") val sparkline: SparklineDto?
)

@JsonClass(generateAdapter = true)
data class SparklineDto(@param:Json(name = "price") val price: List<Double>)
