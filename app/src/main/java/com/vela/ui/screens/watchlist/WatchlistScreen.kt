package com.vela.ui.screens.watchlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.AssetLazyList
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.ScreenHeader
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.common.util.SharedTransitionWrapper
import kotlinx.coroutines.flow.flowOf

internal data class WatchlistActions(val assetListItemActions: AssetListItemActions)

@Composable
fun WatchlistRoute(
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {
    ScreenVisibilityObserver(viewModel::onScreenVisible)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val watchlistActions = WatchlistActions(
        assetListItemActions = viewModel.assetListItemActions(onClick = navigateToDetail)
    )

    WatchlistScreen(
        uiState = uiState,
        watchlistActions = watchlistActions,
        modifier = modifier
    )
}

@Composable
internal fun WatchlistScreen(
    uiState: WatchlistUiState,
    watchlistActions: WatchlistActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        NonBlockingErrorBanner(error = (uiState as? WatchlistUiState.Success)?.nonBlockingError)
        ScreenHeader(title = stringResource(R.string.title_watchlist))
        when (uiState) {
            is WatchlistUiState.Loading -> LoadingState()

            is WatchlistUiState.Empty -> EmptyState()

            is WatchlistUiState.Success ->
                SuccessState(
                    uiState = uiState,
                    watchlistActions = watchlistActions
                )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    FullScreenLoader(modifier = modifier)
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.label_watchlist_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.label_watchlist_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SuccessState(
    uiState: WatchlistUiState.Success,
    watchlistActions: WatchlistActions,
    modifier: Modifier = Modifier
) {
    AssetLazyList(
        assets = uiState.assets,
        actions = watchlistActions.assetListItemActions,
        modifier = modifier.fillMaxSize()
    )
}

// ---- Previews ----

@Composable
private fun WatchlistPreview(uiState: WatchlistUiState) {
    SharedTransitionWrapper {
        WatchlistScreen(
            uiState = uiState,
            watchlistActions = WatchlistActions(
                assetListItemActions = AssetListItemActions(
                    observePrice = { flowOf(null) },
                    observeIsWatchlisted = { flowOf(false) },
                    onToggleWatchlist = {},
                    onClick = {}
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchlistLoadingPreview() {
    WatchlistPreview(WatchlistUiState.Loading)
}

@Preview(showBackground = true)
@Composable
private fun WatchlistEmptyPreview() {
    WatchlistPreview(WatchlistUiState.Empty)
}

@Preview(showBackground = true)
@Composable
private fun WatchlistSuccessPreview() {
    WatchlistPreview(
        uiState = WatchlistUiState.Success(
            assets = FakeAssetDataSource.assets.map { it.toUiModel() }
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun WatchlistNonBlockingErrorPreview() {
    WatchlistPreview(
        uiState = WatchlistUiState.Success(
            assets = FakeAssetDataSource.assets.map { it.toUiModel() },
            nonBlockingError = AppError.NoInternet
        )
    )
}
