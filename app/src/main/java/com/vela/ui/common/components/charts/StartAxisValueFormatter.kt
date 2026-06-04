package com.vela.ui.common.components.charts

import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import java.util.Locale

internal val StartAxisValueFormatter = CartesianValueFormatter { context, value, _ ->
    val candlestickModel = context.model.models.filterIsInstance<CandlestickCartesianLayerModel>().firstOrNull()
    val range = candlestickModel?.let { it.maxY - it.minY } ?: 0.0

    when {
        value >= 1000000 -> String.format(Locale.US, "$%.1fM", value / 1000000)
        value >= 1000 -> {
            val thousands = value / 1000
            when {
                range >= 10000 -> String.format(Locale.US, "$%.0fk", thousands)
                range >= 1000 -> String.format(Locale.US, "$%.1fk", thousands)
                else -> String.format(Locale.US, "$%.2fk", thousands)
            }
        }

        range >= 10 -> String.format(Locale.US, "$%.0f", value)
        range >= 0.1 -> String.format(Locale.US, "$%.2f", value)
        else -> String.format(Locale.US, "$%.4f", value)
    }
}