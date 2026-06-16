package com.vela.ui.common.components.mapper

import com.vela.domain.model.Asset
import com.vela.ui.theme.sparklineBearColor
import com.vela.ui.theme.sparklineBullColor
import org.junit.Assert.assertEquals
import org.junit.Test

class AssetMapperKtTest {
    private fun anAsset(
        currentPrice: Double = 100.0,
        priceChangePercent24h: Double = 1.0,
        symbol: String = "btc",
        marketCapRank: Int = 1,
    ) = Asset(
        id = "bitcoin",
        symbol = symbol,
        name = "Bitcoin",
        image = "",
        currentPrice = currentPrice,
        priceChangePercent24h = priceChangePercent24h,
        marketCapRank = marketCapRank,
        sparkline = listOf(1.0, 2.0, 3.0)
    )

    @Test
    fun `Asset basic properties mapping`() {
        val asset = anAsset()
        val uiModel = asset.toUiModel()

        assertEquals(asset.id, uiModel.id)
        assertEquals(asset.name, uiModel.name)
        assertEquals(asset.image, uiModel.image)
        assertEquals(asset.sparkline, uiModel.sparkline)
    }

    @Test
    fun `Price formatting for values over 1000`() {
        val uiModel = anAsset(currentPrice = 67420.0).toUiModel()
        assertEquals("$67,420", uiModel.price)
    }

    @Test
    fun `Price formatting for values exactly 1000`() {
        val uiModel = anAsset(currentPrice = 1000.0).toUiModel()
        assertEquals("$1,000", uiModel.price)
    }

    @Test
    fun `Price formatting for values between 1 and 1000`() {
        val uiModel = anAsset(currentPrice = 38.20).toUiModel()
        assertEquals("$38.20", uiModel.price)
    }

    @Test
    fun `Price formatting for values exactly 1`() {
        val uiModel = anAsset(currentPrice = 1.0).toUiModel()
        assertEquals("$1.00", uiModel.price)
    }

    @Test
    fun `Price formatting for very small values`() {
        val uiModel = anAsset(currentPrice = 0.00000043).toUiModel()
        assertEquals("$0.00000043", uiModel.price)
    }

    @Test
    fun `Price formatting for values below 1`() {
        val uiModel = anAsset(currentPrice = 0.52).toUiModel()
        assertEquals("$0.52", uiModel.price)
    }

    @Test
    fun `Price formatting for zero value`() {
        val uiModel = anAsset(currentPrice = 0.0).toUiModel()
        assertEquals("$0.00", uiModel.price)
    }

    @Test
    fun `Price change formatting for positive values`() {
        val uiModel = anAsset(priceChangePercent24h = 2.4).toUiModel()
        assertEquals("+2.40%", uiModel.priceChange)
    }

    @Test
    fun `Price change formatting for negative values`() {
        val uiModel = anAsset(priceChangePercent24h = -1.1).toUiModel()
        assertEquals("-1.10%", uiModel.priceChange)
    }

    @Test
    fun `Price change formatting for zero value`() {
        val uiModel = anAsset(priceChangePercent24h = 0.0).toUiModel()
        assertEquals("0.00%", uiModel.priceChange)
    }

    @Test
    fun `Sparkline color for positive price change`() {
        val uiModel = anAsset(priceChangePercent24h = 2.4).toUiModel()
        assertEquals(sparklineBullColor, uiModel.color)
    }

    @Test
    fun `Sparkline color for negative price change`() {
        val uiModel = anAsset(priceChangePercent24h = -1.1).toUiModel()
        assertEquals(sparklineBearColor, uiModel.color)
    }

    @Test
    fun `Sparkline color for zero price change`() {
        val uiModel = anAsset(priceChangePercent24h = 0.0).toUiModel()
        assertEquals(sparklineBullColor, uiModel.color)
    }

    @Test
    fun `Rounding behavior for high value prices`() {
        val uiModel = anAsset(currentPrice = 1000.55).toUiModel()
        assertEquals("$1,000", uiModel.price)
    }

    @Test
    fun `Rounding behavior for percentage changes`() {
        val uiModel = anAsset(priceChangePercent24h = 0.1234).toUiModel()
        assertEquals("+0.12%", uiModel.priceChange)
    }

    @Test
    fun `Symbol and Rank string construction formatting`() {
        val uiModel = anAsset(symbol = "btc", marketCapRank = 1).toUiModel()
        assertEquals("#1 · BTC", uiModel.symbolAndRank)
    }

    @Test
    fun `Symbol casing in symbolAndRank mapping`() {
        val uiModel = anAsset(symbol = "eth").toUiModel()
        assertEquals("#1 · ETH", uiModel.symbolAndRank)
    }

    @Test
    fun `Price formatting for values between 0 0001 and 1 0`() {
        // 0.0001 → 4 decimals after trim, falls back to %.4f → $0.0001
        val uiModel = anAsset(currentPrice = 0.0001).toUiModel()
        assertEquals("$0.0001", uiModel.price)
    }

    @Test
    fun `Price formatting boundary check for exactly 4 decimals below 1`() {
        // 0.00012 → trims to "0.00012" → 5 decimals > 4 → returns trimmed → $0.00012
        val uiModel = anAsset(currentPrice = 0.00012).toUiModel()
        assertEquals("$0.00012", uiModel.price)
    }

    @Test
    fun `Price formatting for values with many trailing zeros below 1`() {
        // 0.0001 stored as 0.00010000 → trimEnd('0') → "0.0001" → 4 decimals → $0.0001
        val uiModel = anAsset(currentPrice = 0.00010000).toUiModel()
        assertEquals("$0.0001", uiModel.price)
    }

    @Test
    fun `Price formatting for small values exceeding 8 decimal places`() {
        // 0.000000001 underflows 8 decimal places → formats to "0.00000000" → trims to "0." → 0 decimals → fallback $0.0000
        val uiModel = anAsset(currentPrice = 0.000000001).toUiModel()
        assertEquals("$0.00", uiModel.price)
    }

    @Test
    fun `Price formatting for values between 1 and 1000 with more than 2 decimal places`() {
        val uiModel = anAsset(currentPrice = 1.234).toUiModel()
        assertEquals("$1.23", uiModel.price)
    }

    @Test
    fun `Price formatting for high values with decimal rounding`() {
        val uiModel = anAsset(currentPrice = 1234.5).toUiModel()
        assertEquals("$1,234", uiModel.price)
    }

    @Test
    fun `Price change percentage rounding to nearest hundredth`() {
        val uiModel = anAsset(priceChangePercent24h = 1.236).toUiModel()
        assertEquals("+1.24%", uiModel.priceChange)
    }

    @Test
    fun `Empty or Null like string handling for ID and Name`() {
        val asset = anAsset().copy(id = "", name = "")
        val uiModel = asset.toUiModel()
        assertEquals("", uiModel.id)
        assertEquals("", uiModel.name)
    }

    @Test
    fun `Null handling for image property`() {
        val uiModel = anAsset().copy(image = null).toUiModel()
        assertEquals("", uiModel.image)
    }

    @Test
    fun `Null handling for currentPrice property`() {
        val uiModel = anAsset().copy(currentPrice = null).toUiModel()
        assertEquals("", uiModel.price)
    }

    @Test
    fun `Null handling for priceChangePercent24h property`() {
        val uiModel = anAsset().copy(priceChangePercent24h = null).toUiModel()
        assertEquals("", uiModel.priceChange)
    }

    @Test
    fun `Color mapping when priceChangePercent24h is null`() {
        val uiModel = anAsset().copy(priceChangePercent24h = null).toUiModel()
        assertEquals(sparklineBullColor, uiModel.color)
    }

    @Test
    fun `Null handling for marketCapRank property`() {
        val uiModel = anAsset().copy(marketCapRank = null).toUiModel()
        assertEquals("BTC", uiModel.symbolAndRank)
    }

    @Test
    fun `Null handling for sparkline list`() {
        val uiModel = anAsset().copy(sparkline = null).toUiModel()
        assertEquals(emptyList<Double>(), uiModel.sparkline)
    }

    @Test
    fun `Price formatting for values between 0 1 and 1 0 with rounding`() {
        val uiModel = anAsset(currentPrice = 0.99999).toUiModel()
        assertEquals("$0.99999", uiModel.price)
    }

    @Test
    fun `Negative price handling`() {
        val uiModel = anAsset(currentPrice = -0.5).toUiModel()
        assertEquals("$-0.50", uiModel.price)
    }

    @Test
    fun `Symbol with special characters or numbers`() {
        val uiModel = anAsset(symbol = "1inch!&").toUiModel()
        assertEquals("#1 · 1INCH!&", uiModel.symbolAndRank)
    }

    @Test
    fun `Large marketCapRank handling`() {
        val uiModel = anAsset(marketCapRank = 9999).toUiModel()
        assertEquals("#9999 · BTC", uiModel.symbolAndRank)
    }

    @Test
    fun `Price formatting for values just below 1000`() {
        val uiModel = anAsset(currentPrice = 999.99).toUiModel()
        assertEquals("$999.99", uiModel.price)
    }
}
