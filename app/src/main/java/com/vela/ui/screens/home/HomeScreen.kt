package com.vela.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.theme.VelaTheme

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullRefreshing by viewModel.pullRefreshing.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        pullRefreshing = pullRefreshing,
        selectedLimit = viewModel.selectedLimit,
        onLimitChanged = viewModel::onLimitChanged,
        onRetry = viewModel::retry,
        onPullToRefresh = viewModel::pullToRefresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    pullRefreshing: Boolean,
    selectedLimit: Int,
    onLimitChanged: (Int) -> Unit,
    onRetry: () -> Unit,
    onPullToRefresh: () -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> FullScreenLoader(modifier = modifier)
        is HomeUiState.Error -> FullScreenError(appError = uiState.appError, onRetry = onRetry, modifier = modifier)
        is HomeUiState.Success -> SuccessState(
            uiState = uiState,
            pullRefreshing = pullRefreshing,
            selectedLimit = selectedLimit,
            onLimitChanged = onLimitChanged,
            onPullToRefresh = onPullToRefresh,
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
    onLimitChanged: (Int) -> Unit,
    onPullToRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = pullRefreshing,
        onRefresh = onPullToRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NonBlockingErrorBanner(error = uiState.nonBlockingError)
            HomeHeader(
                date = uiState.formattedDate,
                selectedLimit = selectedLimit,
                onLimitChanged = onLimitChanged
            )
            AssetLazyList(
                assets = uiState.assets,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun HomeHeader(
    date: String,
    selectedLimit: Int,
    onLimitChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.markets),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            LimitChips(
                selectedLimit = selectedLimit,
                onLimitChanged = onLimitChanged
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
private fun HomeScreenSuccessWithErrorPreviewPreview() = HomeScreenPreview(
    HomeUiState.Success(
        assets = FakeAssetDataSource.assets.map { it.toUiModel() },
        nonBlockingError = AppError.NoInternet,
        formattedDate = "Monday, 12 Feb"
    )
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() = HomeScreenPreview(
    uiState = HomeUiState.Error(AppError.NoInternet)
)

@Composable
private fun HomeScreenPreview(uiState: HomeUiState) {
    VelaTheme {
        HomeScreen(
            uiState = uiState,
            pullRefreshing = false,
            selectedLimit = 25,
            onLimitChanged = {},
            onRetry = {},
            onPullToRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LimitChipsPreview() {
    VelaTheme {
        LimitChips(50, {})
    }
}