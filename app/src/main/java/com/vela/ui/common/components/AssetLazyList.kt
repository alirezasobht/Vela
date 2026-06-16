package com.vela.ui.common.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.common.util.SharedTransitionWrapper
import kotlinx.coroutines.flow.flowOf

@Composable
fun AssetLazyList(
    assets: List<AssetUiModel>,
    actions: AssetListItemActions,
    showWatchlistButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(
            items = assets,
            key = { _, asset -> asset.id }
        ) { index, asset ->
            AssetListItem(
                asset = asset,
                actions = actions,
                showWatchlistButton = showWatchlistButton,
                modifier = Modifier.animateItem()
            )
            if (index < assets.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AssetLazyListPreview() {
    SharedTransitionWrapper {
        AssetLazyList(
            assets = FakeAssetDataSource.assets.map { it.toUiModel() },
            actions =
                AssetListItemActions(
                    observePrice = { flowOf(null) },
                    observeIsWatchlisted = { flowOf(false) },
                    onToggleWatchlist = {},
                    onClick = {}
                )
        )
    }
}
