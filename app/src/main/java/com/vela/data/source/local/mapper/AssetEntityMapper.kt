package com.vela.data.source.local.mapper

import com.vela.data.source.local.model.AssetEntity
import com.vela.domain.model.Asset

fun AssetEntity.toDomain(): Asset = Asset(
    id = id,
    symbol = symbol,
    name = name,
    image = image,
    currentPrice = currentPrice,
    priceChangePercent24h = priceChangePercent24h,
    marketCapRank = marketCapRank,
    sparkline = sparkline?.split(",")?.mapNotNull { it.toDoubleOrNull() }?.filter { it > 0 }
)