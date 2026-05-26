package com.vela.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoinDto(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "symbol") val symbol: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "image") val image: String,
    @field:Json(name = "current_price") val currentPrice: Double,
    @field:Json(name = "price_change_percentage_24h") val priceChangePercent24h: Double,
    @field:Json(name = "market_cap_rank") val marketCapRank: Int,
    @field:Json(name = "sparkline_in_7d") val sparkline: SparklineDto?
)

@JsonClass(generateAdapter = true)
data class SparklineDto(
    @field:Json(name = "price") val price: List<Double>
)