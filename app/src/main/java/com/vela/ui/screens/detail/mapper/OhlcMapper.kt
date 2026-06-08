package com.vela.ui.screens.detail.mapper

import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.vela.domain.model.OhlcPoint

val TimestampsKey = ExtraStore.Key<List<Long>>()

fun List<OhlcPoint>.toCandleStickModel(): CandlestickCartesianLayerModel.Partial {
    val validPoints = filter {
        it.open.isFinite() && it.close.isFinite() && it.low.isFinite() && it.high.isFinite()
    }
    return CandlestickCartesianLayerModel.partial(
        opening = validPoints.map { it.open },
        closing = validPoints.map { it.close },
        low = validPoints.map { it.low },
        high = validPoints.map { it.high }
    )
}

fun List<OhlcPoint>.timestamps(): List<Long> =
    filter {
        it.open.isFinite() && it.close.isFinite() && it.low.isFinite() && it.high.isFinite()
    }.map { it.timestamp }