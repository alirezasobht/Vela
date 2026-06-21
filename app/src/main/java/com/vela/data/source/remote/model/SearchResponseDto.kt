package com.vela.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResponseDto(@param:Json(name = "coins") val coins: List<SearchCoinDto>)

@JsonClass(generateAdapter = true)
data class SearchCoinDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "symbol") val symbol: String,
    @param:Json(name = "market_cap_rank") val marketCapRank: Int?,
    @param:Json(name = "thumb") val thumb: String?
)
