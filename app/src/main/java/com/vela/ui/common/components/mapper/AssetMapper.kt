package com.vela.ui.common.components.mapper

import com.vela.domain.model.Asset
import com.vela.domain.model.SimplePrice
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor

fun Asset.toUiModel(): AssetUiModel = AssetUiModel(
    id = id,
    name = name,
    image = image ?: "",
    price = currentPrice?.let { formatPrice(it) } ?: "",
    priceChange = priceChangePercent24h?.let { formatChange(it) } ?: "",
    color = if ((priceChangePercent24h ?: 0.0) >= 0) sparklineBullColor else sparklineBearColor,
    symbolAndRank = if (marketCapRank != null) "#$marketCapRank \u00b7 ${symbol.uppercase()}" else symbol.uppercase(),
    sparkline = sparkline ?: emptyList()
)

fun SimplePrice.toUiModel(): SimplePriceUiModel = SimplePriceUiModel(
    price = price?.let { formatPrice(it) } ?: "",
    priceChange = priceChange?.let { formatChange(it) } ?: "",
    isPositive = (priceChange ?: 0.0) >= 0.0
)

private fun formatPrice(price: Double): String = when {
    price >= 1000 -> "$${"%,.0f".format(price)}"
    price >= 1 -> "$%.2f".format(price)
    else -> {
        val eightDecimals = "%.8f".format(price)
        val trimmed = eightDecimals.trimEnd('0')
        val dotIndex = trimmed.indexOf('.')
        val decimalCount = if (dotIndex >= 0) trimmed.length - dotIndex - 1 else 0
        if (decimalCount <= 4) "$%.4f".format(price)
        else "$$trimmed"
    }
}

private fun formatChange(change: Double): String {
    val sign = if (change >= 0) "+" else ""
    return "$sign${"%.2f".format(change)}%"
}