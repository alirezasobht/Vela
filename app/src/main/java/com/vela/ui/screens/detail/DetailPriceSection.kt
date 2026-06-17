package com.vela.ui.screens.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.data.source.fake.FakeDetailDataSource
import com.vela.ui.screens.detail.state.toUiModel
import com.vela.ui.theme.VelaTheme
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor

@Composable
internal fun PriceSection(
    price: String,
    priceChange24h: String,
    priceChangePercent: String,
    priceColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = price,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$priceChange24h  \u00b7  $priceChangePercent",
            style = MaterialTheme.typography.bodyMedium,
            color = priceColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceSectionPreview() {
    val detail = FakeDetailDataSource.detail.toUiModel()
    VelaTheme {
        PriceSection(
            price = detail.currentPrice,
            priceChange24h = detail.priceChange24h,
            priceChangePercent = detail.priceChangePercent24h,
            priceColor = if (detail.isPositive) sparklineBullColor else sparklineBearColor,
            modifier = Modifier.padding(16.dp)
        )
    }
}
