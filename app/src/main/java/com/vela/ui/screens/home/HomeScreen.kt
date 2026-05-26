package com.vela.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.data.source.local.fake.FakeAssetDataSource
import com.vela.ui.common.components.AssetListItem
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.theme.VelaTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        HomeHeader(date = uiState.date)
        AssetList(assets = uiState.assets)
    }
}

@Composable
private fun HomeHeader(
    date: String,
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
        Text(
            text = "Markets",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
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
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    VelaTheme {
        HomeScreen(
            uiState = HomeUiState(
                assets = FakeAssetDataSource.assets.map { it.toUiModel() },
                date = "Monday, 25 May"
            )
        )
    }
}