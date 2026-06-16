package com.vela.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.ui.common.components.model.SimplePriceUiModel
import com.vela.ui.theme.VelaTheme
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun PriceCell(
    id: String,
    initialPrice: String = "",
    initialPriceChange: String = "",
    initialColor: Color = sparklineBullColor,
    observePrice: (String) -> Flow<SimplePriceUiModel?>,
    modifier: Modifier = Modifier
) {
    val priceFlow = remember(id) { observePrice(id) }
    val simplePrice by priceFlow.collectAsStateWithLifecycle(initialValue = null)

    val price = simplePrice?.price ?: initialPrice
    val priceChange = simplePrice?.priceChange ?: initialPriceChange
    val color =
        simplePrice?.let {
            if (it.isPositive) sparklineBullColor else sparklineBearColor
        } ?: initialColor

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        if (price.isNotEmpty()) {
            Text(
                text = price,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (priceChange.isNotEmpty()) {
            Text(
                text = priceChange,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceCellWithPricePreview() {
    VelaTheme {
        PriceCell(
            id = "bitcoin",
            initialPrice = "$67,420",
            initialPriceChange = "+2.4%",
            initialColor = sparklineBullColor,
            observePrice = { flowOf(null) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceCellLoadingPreview() {
    VelaTheme {
        PriceCell(
            id = "bitcoin",
            observePrice = { flowOf(null) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceCellUpdatedPreview() {
    VelaTheme {
        PriceCell(
            id = "bitcoin",
            initialPrice = "$67,420",
            initialPriceChange = "+2.4%",
            initialColor = sparklineBullColor,
            observePrice = {
                flowOf(
                    SimplePriceUiModel(
                        price = "$68,000",
                        priceChange = "+3.1%",
                        isPositive = true
                    )
                )
            }
        )
    }
}
