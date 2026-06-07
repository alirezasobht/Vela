package com.vela.ui.screens.detail.chart

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.R
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.OhlcPoint
import com.vela.domain.model.TimeRange
import com.vela.ui.common.components.charts.candle.CandleStickChart
import com.vela.ui.common.util.LandscapePreview
import com.vela.ui.screens.detail.mapper.timestamps
import com.vela.ui.screens.detail.mapper.toCandleStickModel
import com.vela.ui.theme.VelaTheme

@Composable
fun OhlcChartSection(
    ohlcPoints: List<OhlcPoint>,
    isChartLoading: Boolean,
    selectedRange: TimeRange,
    isFullScreen: Boolean,
    onToggleFullScreen: () -> Unit,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeRangeChips(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected,
                isLandscape = true
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ChartState(
                    ohlcPoints = ohlcPoints,
                    isChartLoading = isChartLoading,
                    selectedTimeRange = selectedRange,
                    isFullScreen = isFullScreen,
                    isLandscape = true,
                    onToggleFullScreen = onToggleFullScreen
                )
            }
        }
    } else {
        Column(modifier = modifier) {
            TimeRangeChips(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected,
                isLandscape = false
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                ChartState(
                    ohlcPoints = ohlcPoints,
                    isChartLoading = isChartLoading,
                    selectedTimeRange = selectedRange,
                    isFullScreen = isFullScreen,
                    isLandscape = false,
                    onToggleFullScreen = onToggleFullScreen
                )
            }
        }
    }
}

@Composable
private fun BoxScope.ChartState(
    ohlcPoints: List<OhlcPoint>,
    isChartLoading: Boolean,
    selectedTimeRange: TimeRange,
    isFullScreen: Boolean,
    isLandscape: Boolean,
    onToggleFullScreen: () -> Unit
) {
    when {
        isChartLoading -> LoadingChartContent(modifier = Modifier.align(Alignment.Center))
        ohlcPoints.isNotEmpty() -> ChartContent(ohlcPoints, selectedTimeRange, isLandscape)
        else -> EmptyChartContent(modifier = Modifier.align(Alignment.Center))
    }

    if (isLandscape || ohlcPoints.isNotEmpty()) {
        FullScreenToggle(
            onClick = onToggleFullScreen,
            modifier = Modifier.align(Alignment.BottomEnd),
            isFullScreen = isFullScreen
        )
    }
}

@Composable
private fun LoadingChartContent(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp)
    )
}

@Composable
private fun ChartContent(
    ohlcPoints: List<OhlcPoint>,
    timeRange: TimeRange,
    isLandscape: Boolean
) {
    val model = remember(ohlcPoints) { ohlcPoints.toCandleStickModel() }
    val timestamps = remember(ohlcPoints) { ohlcPoints.timestamps() }

    CandleStickChart(
        model = model,
        timestamps = timestamps,
        timeRange = timeRange,
        modifier = Modifier.fillMaxSize(),
        verticalItemsCount = if (isLandscape) 6 else 4
    )
}

@Composable
private fun EmptyChartContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.label_no_chart_data),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

// ----- Previews ------

@Composable
private fun OhlcChartSectionPreview(
    ohlcPoints: List<OhlcPoint> = FakeOhlcDataSource.bitcoinOhlc,
    isLoading: Boolean = false,
    selectedRange: TimeRange = TimeRange.ONE_DAY,
    isFullScreen: Boolean = false
) {
    VelaTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp)
            ) {
                OhlcChartSection(
                    ohlcPoints = ohlcPoints,
                    isChartLoading = isLoading,
                    selectedRange = selectedRange,
                    isFullScreen = isFullScreen,
                    onToggleFullScreen = {},
                    onRangeSelected = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun OhlcChartSectionChartPreview() = OhlcChartSectionPreview()

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun OhlcChartSectionLoadingPreview() = OhlcChartSectionPreview(isLoading = true)

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun OhlcChartSectionEmptyPreview() = OhlcChartSectionPreview(ohlcPoints = emptyList())

@LandscapePreview(showBackground = true)
@Composable
private fun OhlcChartSectionChartLandscapePreview() = OhlcChartSectionPreview(isFullScreen = true)

@LandscapePreview(showBackground = true)
@Composable
private fun OhlcChartSectionLoadingLandscapePreview() = OhlcChartSectionPreview(isFullScreen = true, isLoading = true)

@LandscapePreview(showBackground = true)
@Composable
private fun OhlcChartSectionEmptyLandscapePreview() = OhlcChartSectionPreview(isFullScreen = true, ohlcPoints = emptyList())
