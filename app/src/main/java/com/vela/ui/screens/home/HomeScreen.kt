package com.vela.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.vela.ui.common.components.AssetListItem
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
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
        is HomeUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is HomeUiState.Error -> {
            val message = when ((uiState as HomeUiState.Error).appError) {
                is AppError.NoInternet -> stringResource(R.string.no_internet_connection)
                is AppError.ServerError -> stringResource(R.string.server_error_please_try_again)
                is AppError.Unknown -> stringResource(R.string.error_unknown)
            }
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.try_again))
                }
            }
        }

        is HomeUiState.Success -> {
            PullToRefreshBox(
                isRefreshing = pullRefreshing,
                onRefresh = onPullToRefresh,
                modifier = modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    (uiState as HomeUiState.Success).let { uiState ->
                        NonBlockingErrorBanner(error = uiState.nonBlockingError)
                        HomeHeader(
                            date = uiState.formattedDate,
                            selectedLimit = selectedLimit,
                            onLimitChanged = onLimitChanged
                        )
                        AssetList(assets = uiState.assets)
                    }
                }
            }
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
private fun AssetList(
    assets: List<AssetUiModel>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(
            items = assets,
            key = { it.id }
        ) { asset ->
            AssetListItem(asset = asset)
            if (asset != assets.last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
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

@Composable
fun NonBlockingErrorBanner(
    error: AppError?,
    modifier: Modifier = Modifier
) {
    var lastError by remember { mutableStateOf(error) }
    if (error != null) lastError = error

    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        lastError?.let { activeError ->
            val message = when (activeError) {
                is AppError.NoInternet -> stringResource(R.string.no_internet_connection)
                is AppError.ServerError -> stringResource(R.string.server_error_please_try_again)
                is AppError.Unknown -> stringResource(R.string.error_unknown)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

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
private fun HomeScreenErrorPreview() = HomeScreenPreview(
    uiState = HomeUiState.Error(AppError.NoInternet)
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() = HomeScreenPreview(
    uiState = HomeUiState.Loading
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
private fun NonBlockingErrorBannerPreview() {
    VelaTheme {
        NonBlockingErrorBanner(error = AppError.NoInternet)
    }
}

@Preview(showBackground = true)
@Composable
private fun LimitChipsPreview() {
    VelaTheme {
        LimitChips(50, {})
    }
}