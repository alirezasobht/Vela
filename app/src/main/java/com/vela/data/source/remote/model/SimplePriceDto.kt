package com.vela.data.source.remote.model

import com.squareup.moshi.Json

data class SimplePriceDto(
    @Json(name = "usd") val usd: Double?,
    @Json(name = "usd_24h_change") val usdChange: Double?
)