package com.vela.data.source.remote.api

import com.vela.data.source.remote.model.SearchCoinDto
import com.vela.domain.model.Asset

fun SearchCoinDto.toDomain(): Asset = Asset(
    id = id,
    symbol = symbol,
    name = name,
    image = thumb,
    currentPrice = null,
    priceChangePercent24h = null,
    marketCapRank = marketCapRank,
    sparkline = null
)