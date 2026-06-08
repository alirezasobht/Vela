package com.vela.ui.screens.detail.state

import com.vela.domain.model.Asset
import com.vela.domain.model.CoinDetail
import com.vela.ui.common.components.mapper.formatChange
import com.vela.ui.common.components.mapper.formatLargeNumber
import com.vela.ui.common.components.mapper.formatPrice
import com.vela.ui.common.components.mapper.formatSupply
import kotlin.math.abs

fun CoinDetail.toUiModel(): CoinDetailUiModel = CoinDetailUiModel(
    id = id,
    name = name,
    symbol = symbol.uppercase(),
    image = image,
    currentPrice = currentPrice?.let { formatPrice(it) } ?: "—",
    priceChange24h = priceChange24h?.let {
        val formatted = formatPrice(abs(it))
        if (it >= 0) "+$formatted" else "-$formatted"
    } ?: "—",
    priceChangePercent24h = priceChangePercent24h?.let { formatChange(it) } ?: "—",
    isPositive = (priceChangePercent24h ?: 0.0) >= 0.0,
    marketCap = marketCap?.let { formatLargeNumber(it) } ?: "—",
    totalVolume = totalVolume?.let { formatLargeNumber(it) } ?: "—",
    circulatingSupply = circulatingSupply?.let { formatSupply(it, symbol.uppercase()) } ?: "—",
    ath = ath?.let { formatPrice(it) } ?: "—",
    atl = atl?.let { formatPrice(it) } ?: "—",
    marketCapRank = marketCapRank
)

fun Asset.toCoinDetailUiModel(): CoinDetailUiModel = CoinDetailUiModel(
    id = id,
    name = name,
    symbol = symbol.uppercase(),
    image = image,
    currentPrice = "",
    priceChange24h = "",
    priceChangePercent24h = "",
    isPositive = true,
    marketCap = "",
    totalVolume = "",
    circulatingSupply = "",
    ath = "",
    atl = "",
    marketCapRank = marketCapRank
)