package com.vela.data.source.remote.mapper

import com.vela.data.source.remote.model.CoinDto
import com.vela.domain.model.Asset

fun CoinDto.toDomain(): Asset = Asset(
    id = id,
    symbol = symbol,
    name = name,
    image = image,
    currentPrice = currentPrice,
    priceChangePercent24h = priceChangePercent24h,
    marketCapRank = marketCapRank,
    sparkline = sparkline?.price
)