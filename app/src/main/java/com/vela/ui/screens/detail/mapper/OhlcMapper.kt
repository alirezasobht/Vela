package com.vela.ui.screens.detail.mapper

import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.vela.domain.model.OhlcPoint

val TimestampsKey = ExtraStore.Key<List<Long>>()

fun List<OhlcPoint>.toCandleStickModel(): CandlestickCartesianLayerModel.Partial =
    CandlestickCartesianLayerModel.partial(
        opening = map { it.open },
        closing = map { it.close },
        low = map { it.low },
        high = map { it.high }
    )

fun List<OhlcPoint>.timestamps(): List<Long> = map { it.timestamp }