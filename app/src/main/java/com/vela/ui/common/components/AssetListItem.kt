package com.vela.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.theme.VelaTheme

@Composable
fun AssetListItem(
    asset: AssetUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Logo
        AsyncImage(
            model = asset.image,
            contentDescription = asset.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            placeholder = rememberVectorPainter(Icons.Default.Paid),
            error = rememberVectorPainter(Icons.Default.MonetizationOn)
        )

        // Name + rank
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = asset.symbolAndRank,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Sparkline
        Sparkline(
            prices = asset.sparkline,
            color = asset.color,
            modifier = Modifier.size(width = 60.dp, height = 30.dp)
        )

        // Price + change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = asset.price,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = asset.priceChange,
                style = MaterialTheme.typography.bodySmall,
                color = asset.color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AssetListItemPreview() {
    VelaTheme {
        AssetListItem(asset = FakeAssetDataSource.assets.first().toUiModel())
    }
}