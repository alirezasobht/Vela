package com.vela.data.source.remote.mapper

import com.vela.data.source.local.model.AssetEntity
import com.vela.data.source.remote.model.CategoryDto
import com.vela.data.source.remote.model.CoinDetailDto
import com.vela.data.source.remote.model.CoinDto
import com.vela.data.source.remote.model.SearchCoinDto
import com.vela.data.source.remote.model.SimplePriceDto
import com.vela.domain.model.Asset
import com.vela.domain.model.CoinDetail
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.OhlcPoint
import com.vela.domain.model.SimplePrice

fun CoinDto.toDomain(): Asset = Asset(
    id = id,
    symbol = symbol,
    name = name,
    image = image,
    currentPrice = currentPrice,
    priceChangePercent24h = priceChangePercent24h,
    marketCapRank = marketCapRank,
    sparkline = sparkline?.price?.takeLast(14)
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

fun CoinDto.toSimplePrice(): SimplePrice = SimplePrice(
    price = currentPrice,
    priceChange = priceChangePercent24h,
    marketCap = null,
    totalVolume = null
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
    priceChange = usdChange,
    marketCap = usdMarketCap,
    totalVolume = usdVolume
)

fun CoinDetailDto.toDomain(): CoinDetail = CoinDetail(
    id = id,
    symbol = symbol,
    name = name,
    image = image,
    currentPrice = currentPrice,
    priceChange24h = priceChange24h,
    priceChangePercent24h = priceChangePercent24h,
    marketCap = marketCap,
    totalVolume = totalVolume,
    circulatingSupply = circulatingSupply,
    ath = ath,
    atl = atl,
    marketCapRank = marketCapRank
)

fun CoinDetailDto.toSimplePrice(): SimplePrice = SimplePrice(
    price = currentPrice,
    priceChange = priceChangePercent24h,
    marketCap = marketCap,
    totalVolume = totalVolume,
    priceChange24hAbsolute = priceChange24h
)

fun List<Double>.toOhlcPoint(): OhlcPoint = OhlcPoint(
    timestamp = this[0].toLong(),
    open = this[1],
    high = this[2],
    low = this[3],
    close = this[4]
)
