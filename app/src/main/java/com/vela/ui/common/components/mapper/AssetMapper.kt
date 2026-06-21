package com.vela.ui.common.components.mapper

import com.vela.domain.model.Asset
import com.vela.domain.model.SimplePrice
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.truncate

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
    priceChange24h = priceChange24hAbsolute?.let { formatSignedPrice(it) },
    isPositive = (priceChange ?: 0.0) >= 0.0,
    marketCap = marketCap?.let { formatLargeNumber(it) },
    totalVolume = totalVolume?.let { formatLargeNumber(it) }
)

internal fun formatPrice(
    value: Double,
    symbol: String = "$",
    locale: Locale = Locale.US
): String {
    val symbols = DecimalFormatSymbols(locale)
    val pattern = when {
        abs(value) >= 1_000 -> "#,##0"
        abs(value) >= 1 -> "#,##0.00"
        else -> "#,##0.00######"
    }
    val formatted = DecimalFormat(pattern, symbols).format(
        if (abs(value) >= 1_000) truncate(value) else value
    )
    return "$symbol$formatted"
}

internal fun formatSignedPrice(value: Double): String {
    val sign = if (value >= 0) "+" else "-"
    return "$sign${formatPrice(value)}"
}

internal fun formatChange(change: Double): String {
    val sign = if (change > 0) "+" else ""
    return "$sign${"%.2f".format(change)}%"
}

internal fun formatLargeNumber(value: Double): String = when {
    value >= 1_000_000_000_000 -> "${"%.2f".format(value / 1_000_000_000_000)}T"
    value >= 1_000_000_000 -> "${"%.2f".format(value / 1_000_000_000)}B"
    value >= 1_000_000 -> "${"%.2f".format(value / 1_000_000)}M"
    else -> "%.0f".format(value)
}

internal fun formatSupply(
    value: Double,
    symbol: String
): String = when {
    value >= 1_000_000_000 -> "${"%.2f".format(value / 1_000_000_000)}B $symbol"
    value >= 1_000_000 -> "${"%.2f".format(value / 1_000_000)}M $symbol"
    value >= 1_000 -> "${"%.2f".format(value / 1_000)}K $symbol"
    else -> "%.0f $symbol".format(value)
}
