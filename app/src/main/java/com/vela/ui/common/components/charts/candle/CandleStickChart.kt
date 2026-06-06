package com.vela.ui.common.components.charts.candle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.OhlcPoint
import com.vela.ui.common.components.charts.RangeProvider
import com.vela.ui.common.components.charts.StartAxisValueFormatter
import com.vela.ui.screens.detail.mapper.toCandleStickModel
import com.vela.ui.theme.VelaTheme
import kotlinx.coroutines.runBlocking

private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(prefix = "$")

@Composable
fun CandleStickChart(
    model: CandlestickCartesianLayerModel.Partial,
    modifier: Modifier = Modifier,
    verticalItemsCount: Int = 4,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    // Use `runBlocking` only for previews, which don’t support asynchronous execution.
    // https://github.com/patrykandpatrick/vico/blob/8f2aa9e175a358eba2aacc5c4e7f01bbbe663b70/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/GoldPrices.kt
    if (LocalInspectionMode.current) {
        runBlocking {
            modelProducer.runTransaction {
                add(model)
            }
        }
    }

    LaunchedEffect(model) {
        modelProducer.runTransaction {
            add(model)
        }
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberCandlestickCartesianLayer(rangeProvider = RangeProvider),
            startAxis =
                VerticalAxis.rememberStart(
                    valueFormatter = StartAxisValueFormatter,
                    itemPlacer = VerticalAxis.ItemPlacer.count({ verticalItemsCount })
                ),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                // TODO: replace with real date/time labels from OhlcPoint.timestamp
                valueFormatter = { _, _, _ -> "." }
            ),
            marker = rememberOhlcChartMarker(valueFormatter = MarkerValueFormatter, showIndicator = false),
        ),
        modelProducer,
        modifier,
    )
}

@Composable
private fun rememberOhlcChartMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter =
        DefaultCartesianMarker.ValueFormatter.default(),
    showIndicator: Boolean = true,
): CartesianMarker {
    val labelBackgroundShape = MarkerCornerBasedShape(CircleShape)
    val labelBackground =
        rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.background),
            shape = labelBackgroundShape,
            strokeFill = Fill(MaterialTheme.colorScheme.outline),
            strokeThickness = 1.dp,
        )
    val label =
        rememberTextComponent(
            style =
                TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                ),
            padding = Insets(8.dp, 4.dp),
            background = labelBackground,
            minWidth = TextComponent.MinWidth.fixed(40.dp),
        )
    val indicatorFrontComponent =
        rememberShapeComponent(Fill(MaterialTheme.colorScheme.surface), CircleShape)
    val guideline = rememberAxisGuidelineComponent()
    return rememberDefaultCartesianMarker(
        label = label,
        valueFormatter = valueFormatter,
        indicator =
            if (showIndicator) {
                { color ->
                    LayeredComponent(
                        back = ShapeComponent(Fill(color.copy(alpha = 0.15f)), CircleShape),
                        front =
                            LayeredComponent(
                                back = ShapeComponent(fill = Fill(color), shape = CircleShape),
                                front = indicatorFrontComponent,
                                padding = Insets(5.dp),
                            ),
                        padding = Insets(10.dp),
                    )
                }
            } else {
                null
            },
        indicatorSize = 36.dp,
        guideline = guideline,
    )
}

// ------ Previews -------

@Composable
private fun ChartPreview(ohlcPoints: List<OhlcPoint>) {
    VelaTheme {
        CandleStickChart(
            model = ohlcPoints.toCandleStickModel(),
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
                .padding(horizontal = 16.dp),
        )
    }
}

@Preview
@Composable
private fun BitcoinChartPreview() = ChartPreview(FakeOhlcDataSource.bitcoinOhlc)

@Preview
@Composable
private fun UsdcChartPreview() = ChartPreview(FakeOhlcDataSource.usdcOhlc)

@Preview
@Composable
private fun EthChartPreview() = ChartPreview(FakeOhlcDataSource.ethOhlc)
