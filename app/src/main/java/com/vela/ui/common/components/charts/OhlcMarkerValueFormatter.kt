package com.vela.ui.common.components.charts

import com.patrykandpatrick.vico.compose.cartesian.marker.CandlestickCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun ohlcMarkerValueFormatter(timestamps: List<Long>): DefaultCartesianMarker.ValueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
    val target = targets.firstOrNull() as? CandlestickCartesianLayerMarkerTarget
        ?: return@ValueFormatter ""
    val index = target.x.toInt()
    val timestamp = timestamps.getOrNull(index)

    val dateStr = timestamp?.let {
        val instant = if (it > 10_000_000_000L) {
            Instant.ofEpochMilli(it)
        } else {
            Instant.ofEpochSecond(it)
        }
        DateTimeFormatter
            .ofPattern("MMM d, HH:mm")
            .format(instant.atZone(ZoneId.systemDefault()))
    } ?: ""

    buildString {
        if (dateStr.isNotEmpty()) appendLine(dateStr)
        appendLine("O: \$${formatMarkerPrice(target.entry.opening)}")
        appendLine("H: \$${formatMarkerPrice(target.entry.high)}")
        appendLine("L: \$${formatMarkerPrice(target.entry.low)}")
        append("C: \$${formatMarkerPrice(target.entry.closing)}")
    }
}

private fun formatMarkerPrice(value: Double): String = when {
    value >= 1000 -> "%,.0f".format(value)
    value >= 1 -> "%.2f".format(value)
    else -> "%.6f".format(value)
}
