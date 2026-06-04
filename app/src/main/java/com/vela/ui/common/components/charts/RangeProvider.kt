package com.vela.ui.common.components.charts

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

internal val RangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val difference = maxY - minY
        return if (difference > 0) minY - (difference * 0.1) else minY * 0.9
    }

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val difference = maxY - minY
        return if (difference > 0) maxY + (difference * 0.1) else maxY * 1.1
    }
}