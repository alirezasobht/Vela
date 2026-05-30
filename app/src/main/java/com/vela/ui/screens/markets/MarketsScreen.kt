package com.vela.ui.screens.markets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.data.source.fake.FakeCategoryDataSource
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.ui.common.components.AssetLazyList
import com.vela.ui.common.components.ScreenHeader
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.theme.VelaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    categories: List<MarketCategory>,
    selectedCategory: MarketCategory,
    sorts: List<MarketSort>,
    selectedSort: MarketSort,
    assets: List<AssetUiModel>,
    onCategorySelected: (MarketCategory) -> Unit,
    onSortSelected: (MarketSort) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ScreenHeader(
            title = stringResource(R.string.explore),
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

        AssetLazyList(
            assets = assets,
            modifier = Modifier.fillMaxSize()
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
                    onCategorySelected(it)
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
                    onSortSelected(it)
                    showSortSheet = false
                }
            )
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
        LazyColumn {
            items(categories) { category ->
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

@Preview(showBackground = true)
@Composable
private fun MarketsScreenPreview() {
    VelaTheme {
        MarketsScreen(
            categories = FakeCategoryDataSource.categories,
            selectedCategory = MarketCategory.ALL,
            sorts = MarketSort.entries,
            selectedSort = MarketSort.MARKET_CAP,
            assets = FakeAssetDataSource.assets.map { it.toUiModel() },
            onCategorySelected = {},
            onSortSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarketsScreenCategorySelectedPreview() {
    VelaTheme {
        MarketsScreen(
            categories = FakeCategoryDataSource.categories,
            selectedCategory = FakeCategoryDataSource.categories[1],
            sorts = MarketSort.entries,
            selectedSort = MarketSort.VOLUME,
            assets = FakeAssetDataSource.assets.map { it.toUiModel() },
            onCategorySelected = {},
            onSortSelected = {}
        )
    }
}

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

@Preview(showBackground = true)
@Composable
private fun SortBottomSheetPreview() {
    VelaTheme {
        SortBottomSheet(
            sorts = MarketSort.entries,
            selectedSort = MarketSort.MARKET_CAP,
            onSortSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetItemSelectedPreview() {
    VelaTheme {
        BottomSheetItem(
            label = "Layer 1",
            selected = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetItemUnselectedPreview() {
    VelaTheme {
        BottomSheetItem(
            label = "DeFi",
            selected = false,
            onClick = {}
        )
    }
}