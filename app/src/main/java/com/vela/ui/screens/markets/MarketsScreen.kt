@file:OptIn(ExperimentalMaterial3Api::class)

package com.vela.ui.screens.markets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.data.source.fake.FakeCategoryDataSource
import com.vela.domain.model.AppError
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.ui.common.components.AssetListItem
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.ScreenHeader
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.theme.VelaTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private data class MarketActions(
    val onCategorySelected: (MarketCategory) -> Unit,
    val onSortSelected: (MarketSort) -> Unit,
    val onRetry: () -> Unit,
    val observePrice: (String) -> Flow<SimplePriceUiModel?>,
    val navigateToDetail: (String) -> Unit
)

@Composable
fun MarketsRoute(
    modifier: Modifier = Modifier,
    viewModel: MarketsViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {

    ScreenVisibilityObserver(viewModel::onScreenVisible)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingFlow.collectAsLazyPagingItems()

    LaunchedEffect(pagingItems.loadState) {
        viewModel.onLoadStateChanged(pagingItems.loadState)
    }

    val marketAction = MarketActions(
        onCategorySelected = viewModel::onCategorySelected,
        onSortSelected = viewModel::onSortSelected,
        onRetry = { pagingItems.retry(); viewModel.retry() },
        observePrice = viewModel::observePrice,
        navigateToDetail = navigateToDetail
    )

    MarketsScreen(
        uiState = uiState,
        categories = categories,
        pagingItems = pagingItems,
        selectedCategory = viewModel.selectedCategory,
        selectedSort = viewModel.selectedSort,
        sorts = MarketSort.entries,
        marketAction = marketAction,
        modifier = modifier
    )
}


@Composable
private fun MarketsScreen(
    uiState: MarketsUiState,
    categories: List<MarketCategory>,
    selectedCategory: MarketCategory,
    sorts: List<MarketSort>,
    selectedSort: MarketSort,
    pagingItems: LazyPagingItems<AssetUiModel>,
    marketAction: MarketActions,
    modifier: Modifier = Modifier
) {

    var showCategorySheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        NonBlockingErrorBanner(error = (uiState as? MarketsUiState.Success)?.nonBlockingError)

        ScreenHeader(
            title = stringResource(R.string.markets),
            controlsRow2 = {
                InputChip(
                    selected = false,
                    onClick = { showCategorySheet = true },
                    label = { Text(selectedCategory.displayName) },
                    leadingIcon = {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                    }
                )
                InputChip(
                    selected = false,
                    onClick = { showSortSheet = true },
                    label = { Text(selectedSort.toDisplayName()) },
                    leadingIcon = {
                        Icon(Icons.Default.Sort, contentDescription = null)
                    }
                )
            }
        )

        when (uiState) {
            is MarketsUiState.Loading -> FullScreenLoader(modifier = modifier)
            is MarketsUiState.Error -> FullScreenError(
                appError = uiState.appError,
                onRetry = marketAction.onRetry,
                modifier = modifier
            )

            is MarketsUiState.Success -> SuccessState(
                uiState = uiState,
                pagingItems = pagingItems,
                marketAction = marketAction,
                modifier = modifier
            )
        }

        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                CategoryBottomSheet(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = {
                        marketAction.onCategorySelected(it)
                        showCategorySheet = false
                    }
                )
            }
        }

        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                SortBottomSheet(
                    sorts = sorts,
                    selectedSort = selectedSort,
                    onSortSelected = {
                        marketAction.onSortSelected(it)
                        showSortSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SuccessState(
    uiState: MarketsUiState.Success,
    pagingItems: LazyPagingItems<AssetUiModel>,
    marketAction: MarketActions,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

    PagedAssetList(
            pagingItems = pagingItems,
            isLoadingMore = uiState.isLoadingMore,
        observePrice = marketAction.observePrice,
        onItemClick = marketAction.navigateToDetail
        )
    }
}

@Composable
private fun PagedAssetList(
    pagingItems: LazyPagingItems<AssetUiModel>,
    isLoadingMore: Boolean,
    observePrice: (String) -> Flow<SimplePriceUiModel?>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id }
        ) { index ->
            pagingItems[index]?.let { asset ->
                AssetListItem(asset = asset, observePrice = observePrice, onClick = onItemClick)
                if (index < pagingItems.itemCount - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryBottomSheet(
    categories: List<MarketCategory>,
    selectedCategory: MarketCategory,
    onCategorySelected: (MarketCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.category),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column {
            categories.forEach { category ->
                BottomSheetItem(
                    label = category.displayName,
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SortBottomSheet(
    sorts: List<MarketSort>,
    selectedSort: MarketSort,
    onSortSelected: (MarketSort) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.sort_by),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        sorts.forEach { sort ->
            BottomSheetItem(
                label = sort.toDisplayName(),
                selected = sort == selectedSort,
                onClick = { onSortSelected(sort) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BottomSheetItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
}

@Composable
private fun MarketSort.toDisplayName(): String = when (this) {
    MarketSort.MARKET_CAP -> stringResource(R.string.sort_market_cap)
    MarketSort.VOLUME -> stringResource(R.string.sort_volume)
}

// ---- Previews ----

@Composable
private fun MarketsScreenPreview(
    uiState: MarketsUiState,
    categories: List<MarketCategory> = FakeCategoryDataSource.categories
) {
    VelaTheme {
        MarketsScreen(
            uiState = uiState,
            categories = categories,
            pagingItems = flowOf(
                PagingData.from(FakeAssetDataSource.assets.map { it.toUiModel() })
            ).collectAsLazyPagingItems(),
            selectedCategory = MarketCategory.ALL,
            selectedSort = MarketSort.MARKET_CAP,
            sorts = MarketSort.entries,
            marketAction = MarketActions(
                onCategorySelected = {},
                onSortSelected = {},
                onRetry = {},
                observePrice = { flowOf(null) },
                navigateToDetail = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarketsScreenLoadingPreview() =
    MarketsScreenPreview(MarketsUiState.Loading)

@Preview(showBackground = true)
@Composable
private fun MarketsScreenErrorPreview() =
    MarketsScreenPreview(MarketsUiState.Error(AppError.NoInternet))

@Preview(showBackground = true)
@Composable
private fun MarketsScreenSuccessPreview() =
    MarketsScreenPreview(MarketsUiState.Success())

@Preview(showBackground = true)
@Composable
private fun MarketsScreenSuccessLoadingMorePreview() =
    MarketsScreenPreview(MarketsUiState.Success(isLoadingMore = true))

@Preview(showBackground = true)
@Composable
private fun MarketsScreenSuccessWithNonBlockingErrorPreview() =
    MarketsScreenPreview(MarketsUiState.Success(nonBlockingError = AppError.NoInternet))

@Preview(showBackground = true)
@Composable
private fun CategoryBottomSheetPreview() {
    VelaTheme {
        CategoryBottomSheet(
            categories = FakeCategoryDataSource.categories,
            selectedCategory = MarketCategory.ALL,
            onCategorySelected = {}
        )
    }
}