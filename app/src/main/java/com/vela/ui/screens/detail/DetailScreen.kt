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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.vela.domain.model.TimeRange
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.common.util.FullScreenOrientationController
import com.vela.ui.common.util.LandscapePreview
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.common.util.rememberFullScreenOrientationController
import com.vela.ui.screens.alerts.AlertFormRoute
import com.vela.ui.screens.detail.chart.OhlcChartSection
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailTab
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
    val onTabSelected: (DetailTab) -> Unit,
    val onEditAlert: (Long?) -> Unit,
    val onDismissAlertForm: () -> Unit,
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

    var editingAlertId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.alertFormEvent.collect { id ->
            editingAlertId = id
        }
    }

    val detailActions = DetailActions(
        onBack = onBack,
        onRetry = viewModel::retry,
        onRangeSelected = viewModel::onRangeSelected,
        onToggleFullScreen = viewModel::toggleChartFullScreen,
        onToggleWatchlist = viewModel::toggleWatchlist,
        onTabSelected = viewModel::onTabSelected,
        onEditAlert = viewModel::openAlertForm,
        onDismissAlertForm = viewModel::dismissAlertForm,
        observePrice = viewModel::observePrice
    )

    DetailScreen(
        uiState = uiState,
        initialAsset = viewModel.initialHeader,
        selectedRange = viewModel.selectedRange,
        isChartFullScreen = viewModel.isChartFullScreen,
        isWatchlisted = isWatchlisted,
        isAlertFormVisible = viewModel.isAlertFormVisible,
        formSessionId = viewModel.formSessionId,
        editingAlertId = editingAlertId,
        coinId = viewModel.coinId,
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
    isAlertFormVisible: Boolean,
    formSessionId: Int,
    editingAlertId: Long?,
    coinId: String,
    detailActions: DetailActions,
    modifier: Modifier = Modifier,
    orientationController: FullScreenOrientationController = rememberFullScreenOrientationController()
) {
    val headerModel = initialAsset ?: (uiState as? DetailUiState.Success)?.detail

    Column(modifier = modifier.fillMaxSize()) {
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

            is DetailUiState.Error ->
                FullScreenError(
                    appError = uiState.appError,
                    onRetry = detailActions.onRetry
                )

            is DetailUiState.Success ->
                SuccessState(
                    uiState = uiState,
                    selectedRange = selectedRange,
                    isChartFullScreen = isChartFullScreen,
                    isAlertFormVisible = isAlertFormVisible,
                    formSessionId = formSessionId,
                    editingAlertId = editingAlertId,
                    coinId = coinId,
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
    isAlertFormVisible: Boolean,
    formSessionId: Int,
    editingAlertId: Long?,
    coinId: String,
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
        } else {
            onDispose { }
        }
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

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAlertFormVisible) {
                        AlertFormRoute(
                            coinId = coinId,
                            alertId = editingAlertId,
                            initialLabel = uiState.alerts.find { it.id == editingAlertId }?.label,
                            formSessionId = formSessionId,
                            onDismiss = detailActions.onDismissAlertForm
                        )
                    } else {
                        DetailTabRow(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = detailActions.onTabSelected
                        )

                        TabsSection(
                            selectedTab = uiState.selectedTab,
                            alerts = uiState.alerts,
                            detail = detail,
                            marketCap = marketCap,
                            totalVolume = totalVolume,
                            onEditAlert = detailActions.onEditAlert
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
    uiState: DetailUiState,
    isChartFullScreen: Boolean = false,
    isWatchlisted: Boolean = false,
    isAlertFormVisible: Boolean = false
) {
    SharedTransitionWrapper {
        DetailScreen(
            uiState = uiState,
            initialAsset = FakeAssetDataSource.assets[0].toCoinDetailUiModel(),
            selectedRange = TimeRange.ONE_DAY,
            isChartFullScreen = isChartFullScreen,
            isWatchlisted = isWatchlisted,
            isAlertFormVisible = isAlertFormVisible,
            formSessionId = 0,
            editingAlertId = null,
            coinId = "bitcoin",
            detailActions = DetailActions(
                onBack = {},
                onRetry = {},
                onRangeSelected = {},
                onToggleFullScreen = {},
                onToggleWatchlist = {},
                onTabSelected = {},
                onEditAlert = {},
                onDismissAlertForm = {},
                observePrice = { flowOf(null) }
            )
        )
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenLoadingPreview() = DetailScreenPreview(uiState = DetailUiState.Loading)

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenErrorPreview() = DetailScreenPreview(uiState = DetailUiState.Error(AppError.NoInternet))

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenSuccessPreview() = DetailScreenPreview(
    uiState = DetailUiState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc
    )
)

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenSuccessAlertsTabPreview() = DetailScreenPreview(
    uiState = DetailUiState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
        selectedTab = DetailTab.ALERTS
    )
)

@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun DetailScreenSuccessWithNoneBlockingErrorPreview() = DetailScreenPreview(
    uiState = DetailUiState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
        nonBlockingError = AppError.NoInternet
    )
)

@LandscapePreview(showBackground = true)
@Composable
private fun FullScreenChartPreview() = DetailScreenPreview(
    uiState = DetailUiState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
    ),
    isChartFullScreen = true
)
