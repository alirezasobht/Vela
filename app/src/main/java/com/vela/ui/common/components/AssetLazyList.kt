package com.vela.ui.common.components

import androidx.compose.foundation.layout.fillMaxSize
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
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.theme.VelaTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun AssetLazyList(
    assets: List<AssetUiModel>,
    observePrice: (String) -> Flow<SimplePriceUiModel?>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(
            items = assets,
            key = { _, asset -> asset.id }
        ) { index, asset ->
            AssetListItem(asset = asset, observePrice = observePrice, onClick = onItemClick)
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
    VelaTheme {
        AssetLazyList(
            assets = FakeAssetDataSource.assets.map { it.toUiModel() },
            observePrice = { flowOf(null) },
            onItemClick = {}
        )
    }
}