package com.vela.data.source.remote.mapper

import com.vela.data.source.local.model.AssetEntity
import com.vela.data.source.remote.model.CategoryDto
import com.vela.data.source.remote.model.CoinDto
import com.vela.data.source.remote.model.SearchCoinDto
import com.vela.data.source.remote.model.SimplePriceDto
import com.vela.domain.model.Asset
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.SimplePrice

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

fun CoinDto.toEntity(): AssetEntity = AssetEntity(
    id = id,
    symbol = symbol,
    name = name,
    image = image,
    currentPrice = currentPrice,
    priceChangePercent24h = priceChangePercent24h,
    marketCapRank = marketCapRank,
    sparkline = sparkline?.price?.takeLast(14)?.joinToString(",")
)

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

fun CategoryDto.toDomain(): MarketCategory = MarketCategory(
    id = id,
    displayName = name
)

fun SimplePriceDto.toDomain(): SimplePrice = SimplePrice(
    price = usd,
    priceChange = usdChange
)
