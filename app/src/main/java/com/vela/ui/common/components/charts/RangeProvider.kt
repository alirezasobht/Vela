package com.vela.ui.common.components.charts

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

internal val RangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        if (!minY.isFinite() || !maxY.isFinite()) return 0.0
        val difference = maxY - minY
        return if (difference > 0) {
            minY - (difference * 0.1)
        } else if (minY != 0.0) {
            minY * 0.9
        } else {
            -1.0
        }
    }

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        if (!minY.isFinite() || !maxY.isFinite()) return 1.0
        val difference = maxY - minY
        return if (difference > 0) {
            maxY + (difference * 0.1)
        } else if (maxY != 0.0) {
            maxY * 1.1
        } else {
            1.0
        }
    }
}
