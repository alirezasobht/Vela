package com.vela.ui.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.ui.theme.VelaTheme
import com.vela.ui.theme.sparklineBearColor

@Composable
fun Sparkline(
    prices: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {

    Canvas(modifier = modifier) {
        val validPrices = prices.filter { it.isFinite() }
        if (validPrices.size < 2) return@Canvas

        val min = validPrices.min()
        val max = validPrices.max()
        val range = if (max - min == 0.0) 1.0 else max - min

        val path = Path()
        validPrices.forEachIndexed { index, price ->
            val x = index / (validPrices.size - 1).toFloat() * size.width
            val y = ((max - price) / range * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path = path, color = color, style = Stroke(width = 3f))
    }
}


@Preview(showBackground = true)
@Composable
private fun SparklinePreview(
    prices: List<Double> = FakeAssetDataSource.assets[0].sparkline!!,
    color: Color = sparklineBearColor
) {
    VelaTheme {
            Sparkline(
                prices = prices,
                color = color,
                modifier = Modifier.size(width = 60.dp, height = 30.dp)
            )
    }
}