package com.vela.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.data.source.fake.FakeDetailDataSource
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.util.FullScreenOrientationController
import com.vela.ui.common.util.LandscapePreview
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.common.util.rememberFullScreenOrientationController
import com.vela.ui.screens.alerts.AlertFormRoute
import com.vela.ui.screens.detail.chart.OhlcChartSection
import com.vela.ui.screens.detail.state.AlertFormState
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailAction
import com.vela.ui.screens.detail.state.DetailContentState
import com.vela.ui.screens.detail.state.DetailScreenState
import com.vela.ui.screens.detail.state.DetailTab
import com.vela.ui.screens.detail.state.toCoinDetailUiModel
import com.vela.ui.screens.detail.state.toUiModel
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor

@Composable
fun DetailRoute(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    ScreenVisibilityObserver(viewModel::onScreenVisible)

    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailScreen(
        state = state,
        initialAsset = viewModel.initialHeader,
        onAction = { action ->
            if (action is DetailAction.Back) onBack() else viewModel.onAction(action)
        }
    )
}

@Composable
internal fun DetailScreen(
    state: DetailScreenState,
    initialAsset: CoinDetailUiModel?,
    onAction: (DetailAction) -> Unit,
    modifier: Modifier = Modifier,
    orientationController: FullScreenOrientationController = rememberFullScreenOrientationController()
) {
    val coinDetail = initialAsset ?: (state.content as? DetailContentState.Success)?.detail

    Column(modifier = modifier.fillMaxSize()) {
        HeaderSection(
            name = coinDetail?.name ?: "",
            symbol = coinDetail?.symbol ?: "",
            image = coinDetail?.image,
            marketCapRank = coinDetail?.marketCapRank,
            coinId = coinDetail?.id ?: "",
            isWatchlisted = state.isWatchlisted,
            onBack = if (state.isChartFullScreen) {
                { onAction(DetailAction.ToggleFullScreen) }
            } else {
                { onAction(DetailAction.Back) }
            },
            onToggleWatchlist = { onAction(DetailAction.ToggleWatchlist) }
        )

        when (val content = state.content) {
            is DetailContentState.Loading -> FullScreenLoader()

            is DetailContentState.Error -> FullScreenError(
                appError = content.appError,
                onRetry = { onAction(DetailAction.Retry) }
            )

            is DetailContentState.Success -> SuccessState(
                state = state,
                content = content,
                onAction = onAction,
                orientationController = orientationController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessState(
    state: DetailScreenState,
    content: DetailContentState.Success,
    onAction: (DetailAction) -> Unit,
    orientationController: FullScreenOrientationController,
    modifier: Modifier = Modifier
) {
    val detail = content.detail
    val priceColor = if (detail.isPositive) sparklineBullColor else sparklineBearColor

    BackHandler(enabled = state.isChartFullScreen) {
        onAction(DetailAction.ToggleFullScreen)
    }

    DisposableEffect(state.isChartFullScreen) {
        if (state.isChartFullScreen) {
            val restoreOrientation = orientationController.lockLandscape()
            onDispose(restoreOrientation)
        } else {
            onDispose { }
        }
    }

    AnimatedContent(
        targetState = state.isChartFullScreen,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(300)) using SizeTransform(clip = false)
        },
        label = "FullScreenTransition",
        modifier = modifier.fillMaxSize()
    ) { fullScreen ->
        if (fullScreen) {
            OhlcChartSection(
                ohlcPoints = content.ohlcPoints,
                isChartLoading = content.isChartLoading,
                selectedRange = state.selectedRange,
                onRangeSelected = { onAction(DetailAction.RangeSelected(it)) },
                isFullScreen = true,
                onToggleFullScreen = { onAction(DetailAction.ToggleFullScreen) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .systemBarsPadding()
                    .layoutId("chart")
            )
        } else {
            PullToRefreshBox(
                isRefreshing = content.isRefreshing,
                onRefresh = { onAction(DetailAction.Retry) },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    NonBlockingErrorBanner(error = content.nonBlockingError)

                    PriceSection(
                        price = detail.currentPrice,
                        priceChange24h = detail.priceChange24h,
                        priceChangePercent = detail.priceChangePercent24h,
                        priceColor = priceColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    OhlcChartSection(
                        ohlcPoints = content.ohlcPoints,
                        isChartLoading = content.isChartLoading,
                        selectedRange = state.selectedRange,
                        isFullScreen = false,
                        onToggleFullScreen = { onAction(DetailAction.ToggleFullScreen) },
                        onRangeSelected = { onAction(DetailAction.RangeSelected(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(216.dp)
                            .padding(horizontal = 16.dp)
                            .layoutId("chart"),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.alertFormState is AlertFormState.Visible) {
                        AlertFormRoute(
                            coinId = state.coinId,
                            alertId = state.alertFormState.alertRowUiModel?.id,
                            initialLabel = state.alertFormState.alertRowUiModel?.label,
                            formSessionId = state.alertFormState.formSessionId,
                            onDismiss = { onAction(DetailAction.DismissAlertForm) }
                        )
                    } else {
                        DetailTabRow(
                            selectedTab = content.selectedTab,
                            onTabSelected = { onAction(DetailAction.TabSelected(it)) }
                        )

                        TabsSection(
                            selectedTab = content.selectedTab,
                            alerts = content.alerts,
                            detail = detail,
                            marketCap = detail.marketCap,
                            totalVolume = detail.totalVolume,
                            onEditAlert = { onAction(DetailAction.EditAlert(it)) }
                        )
                    }
                }
            }
        }
    }
}

// ---- Previews ----

@Composable
private fun DetailScreenPreview(
    content: DetailContentState,
    isChartFullScreen: Boolean = false,
    isWatchlisted: Boolean = false,
    alertFormState: AlertFormState = AlertFormState.Invisible
) {
    SharedTransitionWrapper {
        DetailScreen(
            state = DetailScreenState(
                coinId = "bitcoin",
                isChartFullScreen = isChartFullScreen,
                isWatchlisted = isWatchlisted,
                alertFormState = alertFormState,
                content = content
            ),
            initialAsset = FakeAssetDataSource.assets[0].toCoinDetailUiModel(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenLoadingPreview() = DetailScreenPreview(content = DetailContentState.Loading)

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenErrorPreview() = DetailScreenPreview(content = DetailContentState.Error(AppError.NoInternet))

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenSuccessPreview() = DetailScreenPreview(
    content = DetailContentState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc
    )
)

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenSuccessAlertsTabPreview() = DetailScreenPreview(
    content = DetailContentState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
        selectedTab = DetailTab.ALERTS
    )
)

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenSuccessWithNoneBlockingErrorPreview() = DetailScreenPreview(
    content = DetailContentState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
        nonBlockingError = AppError.NoInternet
    )
)

@LandscapePreview(showBackground = true)
@Composable
private fun FullScreenChartPreview() = DetailScreenPreview(
    content = DetailContentState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
    ),
    isChartFullScreen = true
)
