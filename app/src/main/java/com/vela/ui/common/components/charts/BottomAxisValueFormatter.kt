package com.vela.ui.common.components.charts

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.vela.domain.model.TimeRange
import com.vela.ui.screens.detail.mapper.TimestampsKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun bottomAxisValueFormatter(timeRange: TimeRange): CartesianValueFormatter {
    val formatter =
        when (timeRange) {
            TimeRange.ONE_DAY -> DateTimeFormatter.ofPattern("HH:mm")
            TimeRange.SEVEN_DAYS,
            TimeRange.ONE_MONTH -> DateTimeFormatter.ofPattern("MMM d")

            TimeRange.THREE_MONTHS,
            TimeRange.ONE_YEAR -> DateTimeFormatter.ofPattern("MMM yyyy")
        }
    return CartesianValueFormatter { context, value, _ ->
        val timestamps = context.model.extraStore.getOrNull(TimestampsKey)
        val timestamp = timestamps?.getOrNull(value.toInt()) ?: return@CartesianValueFormatter ""
        val instant =
            if (timestamp > 10_000_000_000L) {
                Instant.ofEpochMilli(timestamp)
            } else {
                Instant.ofEpochSecond(timestamp)
            }
        instant
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}
