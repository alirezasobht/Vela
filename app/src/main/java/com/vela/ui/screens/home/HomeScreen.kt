package com.vela.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.AssetLazyList
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.ScreenHeader
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.theme.VelaTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal data class HomeActions(
    val onLimitChanged: (Int) -> Unit,
    val onRetry: () -> Unit,
    val onPullToRefresh: () -> Unit,
    val observePrice: (String) -> Flow<SimplePriceUiModel?>,
    val onAssetClick: (String) -> Unit
)

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {

    ScreenVisibilityObserver(viewModel::onScreenVisible)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullRefreshing by viewModel.pullRefreshing.collectAsStateWithLifecycle()

    val homeActions = HomeActions(
        onLimitChanged = viewModel::onLimitChanged,
        onRetry = viewModel::retry,
        onPullToRefresh = viewModel::pullToRefresh,
        observePrice = viewModel::observePrice,
        onAssetClick = navigateToDetail
    )

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        pullRefreshing = pullRefreshing,
        selectedLimit = viewModel.selectedLimit,
        homeActions = homeActions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    pullRefreshing: Boolean,
    selectedLimit: Int,
    homeActions: HomeActions
) {
    when (uiState) {
        is HomeUiState.Loading -> FullScreenLoader(modifier = modifier)
        is HomeUiState.Error -> FullScreenError(appError = uiState.appError, onRetry = homeActions.onRetry, modifier = modifier)
        is HomeUiState.Success -> SuccessState(
            uiState = uiState,
            pullRefreshing = pullRefreshing,
            selectedLimit = selectedLimit,
            homeActions = homeActions,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessState(
    uiState: HomeUiState.Success,
    pullRefreshing: Boolean,
    selectedLimit: Int,
    homeActions: HomeActions,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = pullRefreshing,
        onRefresh = homeActions.onPullToRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = modifier.fillMaxSize()) {
            NonBlockingErrorBanner(error = uiState.nonBlockingError)
            ScreenHeader(
                title = stringResource(R.string.title_markets),
                subtitle = uiState.formattedDate,
                controlsRow1 = {
                    LimitChips(
                        selectedLimit = selectedLimit,
                        onLimitChanged = homeActions.onLimitChanged
                    )
                }
            )
            AssetLazyList(
                assets = uiState.assets,
                observePrice = homeActions.observePrice,
                onItemClick = homeActions.onAssetClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LimitChips(
    selectedLimit: Int,
    onLimitChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val limits = listOf(25, 50, 100)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        limits.forEach { limit ->
            FilterChip(
                selected = selectedLimit == limit,
                onClick = { onLimitChanged(limit) },
                label = { Text(text = limit.toString()) }
            )
        }
    }
}

// ---- Previews ----

@Composable
private fun HomeScreenPreview(uiState: HomeUiState) {
    VelaTheme {
        HomeScreen(
            uiState = uiState,
            pullRefreshing = false,
            selectedLimit = 25,
            homeActions = HomeActions(
                onLimitChanged = {},
                onRetry = {},
                onPullToRefresh = {},
                observePrice = { flowOf(null) },
                onAssetClick = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() = HomeScreenPreview(
    uiState = HomeUiState.Loading
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenSuccessPreview() = HomeScreenPreview(
    HomeUiState.Success(
        assets = FakeAssetDataSource.assets.map { it.toUiModel() },
        formattedDate = "Monday, 12 Feb"
    )
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenSuccessWithErrorPreview() = HomeScreenPreview(
    HomeUiState.Success(
        assets = FakeAssetDataSource.assets.map { it.toUiModel() },
        nonBlockingError = AppError.NoInternet,
        formattedDate = "Monday, 12 Feb"
    )
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() = HomeScreenPreview(
    HomeUiState.Error(AppError.NoInternet)
)

@Preview(showBackground = true)
@Composable
private fun LimitChipsPreview() {
    VelaTheme {
        LimitChips(selectedLimit = 50, onLimitChanged = {})
    }
}