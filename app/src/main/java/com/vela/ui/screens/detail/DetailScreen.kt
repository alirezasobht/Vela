package com.vela.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.data.source.fake.FakeDetailDataSource
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.AppError
import com.vela.domain.model.OhlcPoint
import com.vela.domain.model.TimeRange
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.charts.candle.CandleStickChart
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.screens.detail.mapper.timestamps
import com.vela.ui.screens.detail.mapper.toCandleStickModel
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailUiState
import com.vela.ui.screens.detail.state.toCoinDetailUiModel
import com.vela.ui.screens.detail.state.toUiModel
import com.vela.ui.theme.VelaTheme
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private data class DetailActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onRangeSelected: (TimeRange) -> Unit,
    val observePrice: (String) -> Flow<SimplePriceUiModel?>
)

@Composable
fun DetailRoute(
    coinId: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    ScreenVisibilityObserver(viewModel::onScreenVisible)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val detailActions = DetailActions(
        onBack = onBack,
        onRetry = viewModel::retry,
        onRangeSelected = viewModel::onRangeSelected,
        observePrice = viewModel::observePrice
    )

    DetailScreen(
        uiState = uiState,
        initialAsset = viewModel.initialHeader,
        selectedRange = viewModel.selectedRange,
        detailActions = detailActions
    )
}

@Composable
private fun DetailScreen(
    uiState: DetailUiState,
    initialAsset: CoinDetailUiModel?,
    selectedRange: TimeRange,
    detailActions: DetailActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        HeaderSection(
            name = initialAsset?.name ?: (uiState as? DetailUiState.Success)?.detail?.name ?: "",
            symbol = initialAsset?.symbol ?: (uiState as? DetailUiState.Success)?.detail?.symbol ?: "",
            image = initialAsset?.image ?: (uiState as? DetailUiState.Success)?.detail?.image ?: "",
            onBack = detailActions.onBack
        )

        when (uiState) {
            is DetailUiState.Loading -> FullScreenLoader()
            is DetailUiState.Error -> FullScreenError(
                appError = uiState.appError,
                onRetry = detailActions.onRetry
            )

            is DetailUiState.Success -> SuccessState(
                uiState = uiState,
                selectedRange = selectedRange,
                detailActions = detailActions
            )
        }
    }
}

@Composable
private fun SuccessState(
    uiState: DetailUiState.Success,
    selectedRange: TimeRange,
    detailActions: DetailActions,
    modifier: Modifier = Modifier
) {
    val detail = uiState.detail
    val priceFlow = remember(detail.id) { detailActions.observePrice(detail.id) }
    val simplePrice by priceFlow.collectAsStateWithLifecycle(initialValue = null)

    val price = simplePrice?.price ?: detail.currentPrice
    val priceChangePercent = simplePrice?.priceChange ?: detail.priceChangePercent24h
    val isPositive = simplePrice?.isPositive ?: detail.isPositive
    val marketCap = simplePrice?.marketCap ?: detail.marketCap
    val totalVolume = simplePrice?.totalVolume ?: detail.totalVolume
    val priceColor = if (isPositive) sparklineBullColor else sparklineBearColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        NonBlockingErrorBanner(error = uiState.nonBlockingError)

        PriceSection(
            price = price,
            priceChange24h = detail.priceChange24h,
            priceChangePercent = priceChangePercent,
            priceColor = priceColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        TimeRangeChips(
            selectedRange = selectedRange,
            onRangeSelected = detailActions.onRangeSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        OhlcChartSection(
            ohlcPoints = uiState.ohlcPoints,
            isLoading = uiState.isChartLoading,
            timeRange = selectedRange,
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
                .padding(horizontal = 16.dp)
        )

        StatsSection(
            marketCap = marketCap,
            totalVolume = totalVolume,
            circulatingSupply = detail.circulatingSupply,
            ath = detail.ath,
            atl = detail.atl,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun HeaderSection(
    name: String,
    symbol: String,
    image: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back)
            )
        }

        AsyncImage(
            model = image,
            contentDescription = name,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            placeholder = rememberVectorPainter(Icons.Default.Paid),
            error = rememberVectorPainter(Icons.Default.MonetizationOn)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = symbol,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = { /* watchlist placeholder */ }) {
            Icon(
                imageVector = Icons.Default.StarOutline,
                contentDescription = stringResource(R.string.cd_watchlist)
            )
        }
    }
}

@Composable
private fun PriceSection(
    price: String,
    priceChange24h: String,
    priceChangePercent: String,
    priceColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = price,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$priceChange24h  ·  $priceChangePercent",
            style = MaterialTheme.typography.bodyMedium,
            color = priceColor
        )
    }
}

@Composable
private fun TimeRangeChips(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) }
            )
        }
    }
}

@Composable
private fun OhlcChartSection(
    ohlcPoints: List<OhlcPoint>,
    isLoading: Boolean,
    timeRange: TimeRange,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
            ohlcPoints.isNotEmpty() -> {
                CandleStickChart(
                    model = ohlcPoints.toCandleStickModel(),
                    timestamps = ohlcPoints.timestamps(),
                    timeRange = timeRange,
                    modifier = Modifier.fillMaxSize(),
                    verticalItemsCount = 4
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    marketCap: String,
    totalVolume: String,
    circulatingSupply: String,
    ath: String,
    atl: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        StatRow(label = stringResource(R.string.label_market_cap), value = marketCap)
        StatRow(label = stringResource(R.string.label_24h_volume), value = totalVolume)
        StatRow(label = stringResource(R.string.label_circulating_supply), value = circulatingSupply)
        StatRow(label = stringResource(R.string.label_ath), value = ath)
        StatRow(label = stringResource(R.string.label_atl), value = atl)
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )
    }
}

// ---- Previews ----

@Composable
private fun DetailScreenPreview(uiState: DetailUiState) {
    VelaTheme {
        DetailScreen(
            uiState = uiState,
            initialAsset = FakeAssetDataSource.assets[0].toCoinDetailUiModel(),
            selectedRange = TimeRange.ONE_DAY,
            detailActions = DetailActions(
                onBack = {},
                onRetry = {},
                onRangeSelected = {},
                observePrice = { flowOf(null) }
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenLoadingPreview() =
    DetailScreenPreview(uiState = DetailUiState.Loading)

@Preview(showBackground = true)
@Composable
private fun DetailScreenErrorPreview() =
    DetailScreenPreview(uiState = DetailUiState.Error(AppError.NoInternet))

@Preview(showBackground = true)
@Composable
private fun DetailScreenSuccessPreview() =
    DetailScreenPreview(
        uiState = DetailUiState.Success(
            detail = FakeDetailDataSource.detail.toUiModel(),
            isChartLoading = false,
            ohlcPoints = FakeOhlcDataSource.bitcoinOhlc
        )
    )

@Preview(showBackground = true)
@Composable
private fun DetailScreenSuccessWithNoneBlockingErrorPreview() =
    DetailScreenPreview(
        uiState = DetailUiState.Success(
            detail = FakeDetailDataSource.detail.toUiModel(),
            isChartLoading = false,
            ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
            nonBlockingError = AppError.NoInternet
        )
    )
