package com.vela.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layoutId
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
import com.vela.domain.model.TimeRange
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.common.util.FullScreenOrientationController
import com.vela.ui.common.util.LandscapePreview
import com.vela.ui.common.util.LocalAnimatedVisibilityScope
import com.vela.ui.common.util.LocalSharedTransitionScope
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.common.util.rememberFullScreenOrientationController
import com.vela.ui.screens.detail.chart.OhlcChartSection
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailUiState
import com.vela.ui.screens.detail.state.toCoinDetailUiModel
import com.vela.ui.screens.detail.state.toUiModel
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal data class DetailActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onRangeSelected: (TimeRange) -> Unit,
    val onToggleFullScreen: () -> Unit,
    val onToggleWatchlist: () -> Unit,
    val observePrice: (String) -> Flow<SimplePriceUiModel?>
)

@Composable
fun DetailRoute(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    ScreenVisibilityObserver(viewModel::onScreenVisible)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWatchlisted by viewModel.isWatchlisted.collectAsStateWithLifecycle()

    val detailActions = DetailActions(
        onBack = onBack,
        onRetry = viewModel::retry,
        onRangeSelected = viewModel::onRangeSelected,
        onToggleFullScreen = viewModel::toggleChartFullScreen,
        onToggleWatchlist = viewModel::toggleWatchlist,
        observePrice = viewModel::observePrice
    )

    DetailScreen(
        uiState = uiState,
        initialAsset = viewModel.initialHeader,
        selectedRange = viewModel.selectedRange,
        isChartFullScreen = viewModel.isChartFullScreen,
        isWatchlisted = isWatchlisted,
        detailActions = detailActions
    )
}

@Composable
internal fun DetailScreen(
    uiState: DetailUiState,
    initialAsset: CoinDetailUiModel?,
    selectedRange: TimeRange,
    isChartFullScreen: Boolean,
    isWatchlisted: Boolean,
    detailActions: DetailActions,
    modifier: Modifier = Modifier,
    orientationController: FullScreenOrientationController = rememberFullScreenOrientationController()
) {
    val headerModel = initialAsset ?: (uiState as? DetailUiState.Success)?.detail

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        HeaderSection(
            name = headerModel?.name ?: "",
            symbol = headerModel?.symbol ?: "",
            image = headerModel?.image,
            marketCapRank = headerModel?.marketCapRank,
            coinId = headerModel?.id ?: "",
            isWatchlisted = isWatchlisted,
            onBack = if (isChartFullScreen) detailActions.onToggleFullScreen else detailActions.onBack,
            onToggleWatchlist = detailActions.onToggleWatchlist
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
                isChartFullScreen = isChartFullScreen,
                detailActions = detailActions,
                orientationController = orientationController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessState(
    uiState: DetailUiState.Success,
    selectedRange: TimeRange,
    isChartFullScreen: Boolean,
    detailActions: DetailActions,
    orientationController: FullScreenOrientationController,
    modifier: Modifier = Modifier
) {
    val detail = uiState.detail
    val priceFlow = remember(detail.id) { detailActions.observePrice(detail.id) }
    val simplePrice by priceFlow.collectAsStateWithLifecycle(initialValue = null)

    val price = simplePrice?.price ?: detail.currentPrice
    val priceChangePercent = simplePrice?.priceChange ?: detail.priceChangePercent24h
    val priceChange24h = simplePrice?.priceChange24h ?: detail.priceChange24h
    val isPositive = simplePrice?.isPositive ?: detail.isPositive
    val marketCap = simplePrice?.marketCap ?: detail.marketCap
    val totalVolume = simplePrice?.totalVolume ?: detail.totalVolume
    val priceColor = if (isPositive) sparklineBullColor else sparklineBearColor

    BackHandler(enabled = isChartFullScreen) {
        detailActions.onToggleFullScreen()
    }

    DisposableEffect(isChartFullScreen) {
        if (isChartFullScreen) {
            val restoreOrientation = orientationController.lockLandscape()
            onDispose(restoreOrientation)
        } else onDispose { }
    }

    AnimatedContent(
        targetState = isChartFullScreen,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(300)) using SizeTransform(clip = false)
        },
        label = "FullScreenTransition",
        modifier = modifier.fillMaxSize()
    ) { fullScreen ->
        if (fullScreen) {
            OhlcChartSection(
                ohlcPoints = uiState.ohlcPoints,
                isChartLoading = uiState.isChartLoading,
                selectedRange = selectedRange,
                onRangeSelected = detailActions.onRangeSelected,
                isFullScreen = true,
                onToggleFullScreen = detailActions.onToggleFullScreen,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .systemBarsPadding()
                    .layoutId("chart")
            )
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = detailActions.onRetry,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    NonBlockingErrorBanner(error = uiState.nonBlockingError)

                    PriceSection(
                        price = price,
                        priceChange24h = priceChange24h,
                        priceChangePercent = priceChangePercent,
                        priceColor = priceColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    OhlcChartSection(
                        ohlcPoints = uiState.ohlcPoints,
                        isChartLoading = uiState.isChartLoading,
                        selectedRange = selectedRange,
                        isFullScreen = false,
                        onToggleFullScreen = detailActions.onToggleFullScreen,
                        onRangeSelected = detailActions.onRangeSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(216.dp)
                            .padding(horizontal = 16.dp)
                            .layoutId("chart"),
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
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HeaderSection(
    name: String,
    symbol: String,
    image: String?,
    marketCapRank: Int?,
    coinId: String,
    isWatchlisted: Boolean,
    onBack: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

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

        with(sharedTransitionScope) {
            AsyncImage(
                model = image,
                contentDescription = name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (animatedVisibilityScope != null) {
                            Modifier.sharedElement(
                                rememberSharedContentState(key = "coin-image-$coinId"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        } else Modifier
                    ),
                placeholder = rememberVectorPainter(Icons.Default.Paid),
                error = rememberVectorPainter(Icons.Default.MonetizationOn)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            with(sharedTransitionScope) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = if (animatedVisibilityScope != null) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "coin-name-$coinId"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    } else Modifier
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                marketCapRank?.let {
                    Text(
                        text = "#$it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        IconButton(onClick = onToggleWatchlist) {
            Icon(
                imageVector = if (isWatchlisted) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = stringResource(R.string.cd_watchlist),
                tint = if (isWatchlisted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
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
            text = "$priceChange24h  \u00b7  $priceChangePercent",
            style = MaterialTheme.typography.bodyMedium,
            color = priceColor
        )
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
private fun DetailScreenPreview(
    uiState: DetailUiState,
    isChartFullScreen: Boolean = false,
    isWatchlisted: Boolean = false
) {
    SharedTransitionWrapper {
        DetailScreen(
            uiState = uiState,
            initialAsset = FakeAssetDataSource.assets[0].toCoinDetailUiModel(),
            selectedRange = TimeRange.ONE_DAY,
            isChartFullScreen = isChartFullScreen,
            isWatchlisted = isWatchlisted,
            detailActions = DetailActions(
                onBack = {},
                onRetry = {},
                onRangeSelected = {},
                onToggleFullScreen = {},
                onToggleWatchlist = {},
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
private fun DetailScreenSuccessWatchlistedPreview() =
    DetailScreenPreview(
        uiState = DetailUiState.Success(
            detail = FakeDetailDataSource.detail.toUiModel(),
            isChartLoading = false,
            ohlcPoints = FakeOhlcDataSource.bitcoinOhlc
        ),
        isWatchlisted = true
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

@LandscapePreview(showBackground = true)
@Composable
private fun FullScreenChartPreview() =
    DetailScreenPreview(
        uiState = DetailUiState.Success(
            detail = FakeDetailDataSource.detail.toUiModel(),
            isChartLoading = false,
            ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
        ),
        isChartFullScreen = true
    )

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun HeaderSectionPreview() {
    SharedTransitionWrapper {
        HeaderSection(
            name = "Bitcoin",
            symbol = "BTC",
            image = null,
            marketCapRank = 1,
            coinId = "bitcoin",
            isWatchlisted = false,
            onBack = {},
            onToggleWatchlist = {}
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun HeaderSectionWatchlistedPreview() {
    SharedTransitionWrapper {
        HeaderSection(
            name = "Bitcoin",
            symbol = "BTC",
            image = null,
            marketCapRank = 1,
            coinId = "bitcoin",
            isWatchlisted = true,
            onBack = {},
            onToggleWatchlist = {}
        )
    }
}