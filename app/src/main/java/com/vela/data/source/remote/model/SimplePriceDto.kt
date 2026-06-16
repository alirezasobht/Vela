package com.vela.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SimplePriceDto(
    @Json(name = "usd") val usd: Double?,
    @Json(name = "usd_24h_change") val usdChange: Double?,
    @Json(name = "usd_market_cap") val usdMarketCap: Double?,
    @Json(name = "usd_24h_vol") val usdVolume: Double?
)
