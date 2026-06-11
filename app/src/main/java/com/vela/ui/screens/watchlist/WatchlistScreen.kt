package com.vela.ui.screens.watchlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.ui.common.components.AssetLazyList
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.ScreenHeader
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.common.util.SharedTransitionWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal data class WatchlistActions(
    val observePrice: (String) -> Flow<SimplePriceUiModel?>,
    val onAssetClick: (String) -> Unit,
    val onRemove: (String) -> Unit
)

@Composable
internal fun WatchlistScreen(
    uiState: WatchlistUiState,
    watchlistActions: WatchlistActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.title_watchlist))
        when (uiState) {
            is WatchlistUiState.Loading -> LoadingState()
            is WatchlistUiState.Empty -> EmptyState()
            is WatchlistUiState.Success -> SuccessState(
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
        observePrice = watchlistActions.observePrice,
        onItemClick = watchlistActions.onAssetClick,
        modifier = modifier.fillMaxSize()
    )
}

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun WatchlistLoadingPreview() {
    SharedTransitionWrapper {
        WatchlistScreen(
            uiState = WatchlistUiState.Loading,
            watchlistActions = WatchlistActions(
                observePrice = { flowOf(null) },
                onAssetClick = {},
                onRemove = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchlistEmptyPreview() {
    SharedTransitionWrapper {
        WatchlistScreen(
            uiState = WatchlistUiState.Empty,
            watchlistActions = WatchlistActions(
                observePrice = { flowOf(null) },
                onAssetClick = {},
                onRemove = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchlistSuccessPreview() {
    SharedTransitionWrapper {
        WatchlistScreen(
            uiState = WatchlistUiState.Success(
                assets = FakeAssetDataSource.assets.map { it.toUiModel() }
            ),
            watchlistActions = WatchlistActions(
                observePrice = { flowOf(null) },
                onAssetClick = {},
                onRemove = {}
            )
        )
    }
}