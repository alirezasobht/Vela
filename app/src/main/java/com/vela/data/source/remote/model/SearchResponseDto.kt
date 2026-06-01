package com.vela.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResponseDto(
    @Json(name = "coins") val coins: List<SearchCoinDto>
)

@JsonClass(generateAdapter = true)
data class SearchCoinDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "market_cap_rank") val marketCapRank: Int?,
    @Json(name = "thumb") val thumb: String?
)