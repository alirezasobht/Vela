package com.vela.data.source.local.mapper

import com.vela.data.source.local.model.AssetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssetEntityMapperKtTest {
    private fun anAssetEntity(sparkline: String? = "1.0,2.0,3.0") = AssetEntity(
        id = "bitcoin",
        symbol = "btc",
        name = "Bitcoin",
        image = "https://example.com/btc.png",
        currentPrice = 67420.0,
        priceChangePercent24h = 2.4,
        marketCapRank = 1,
        sparkline = sparkline
    )

    @Test
    fun `Basic field mapping`() {
        val entity = anAssetEntity()
        val asset = entity.toDomain()

        assertEquals(entity.id, asset.id)
        assertEquals(entity.symbol, asset.symbol)
        assertEquals(entity.name, asset.name)
        assertEquals(entity.image, asset.image)
        assertEquals(entity.currentPrice, asset.currentPrice)
        assertEquals(entity.priceChangePercent24h, asset.priceChangePercent24h)
        assertEquals(entity.marketCapRank, asset.marketCapRank)
    }

    @Test
    fun `Sparkline null maps to null`() {
        val asset = anAssetEntity(sparkline = null).toDomain()
        assertNull(asset.sparkline)
    }

    @Test
    fun `Sparkline valid CSV maps to list of doubles`() {
        val asset = anAssetEntity(sparkline = "1.0,2.0,3.0").toDomain()
        assertEquals(listOf(1.0, 2.0, 3.0), asset.sparkline)
    }

    @Test
    fun `Sparkline single value maps correctly`() {
        val asset = anAssetEntity(sparkline = "42.5").toDomain()
        assertEquals(listOf(42.5), asset.sparkline)
    }

    @Test
    fun `Sparkline empty string maps to empty list`() {
        val asset = anAssetEntity(sparkline = "").toDomain()
        assertEquals(emptyList<Double>(), asset.sparkline)
    }

    @Test
    fun `Sparkline with invalid values filters them out`() {
        val asset = anAssetEntity(sparkline = "1.0,abc,3.0").toDomain()
        assertEquals(listOf(1.0, 3.0), asset.sparkline)
    }

    @Test
    fun `Sparkline with all invalid values maps to empty list`() {
        val asset = anAssetEntity(sparkline = "abc,def,xyz").toDomain()
        assertEquals(emptyList<Double>(), asset.sparkline)
    }

    @Test
    fun `Sparkline with large number of values maps correctly`() {
        val prices = (1..168).map { it.toDouble() }
        val asset = anAssetEntity(sparkline = prices.joinToString(",")).toDomain()
        assertEquals(prices, asset.sparkline)
    }

    @Test
    fun `Nullable currentPrice maps to null`() {
        val asset = anAssetEntity().copy(currentPrice = null).toDomain()
        assertNull(asset.currentPrice)
    }

    @Test
    fun `Nullable priceChangePercent24h maps to null`() {
        val asset = anAssetEntity().copy(priceChangePercent24h = null).toDomain()
        assertNull(asset.priceChangePercent24h)
    }

    @Test
    fun `Nullable marketCapRank maps to null`() {
        val asset = anAssetEntity().copy(marketCapRank = null).toDomain()
        assertNull(asset.marketCapRank)
    }

    @Test
    fun `Nullable image maps to null`() {
        val asset = anAssetEntity().copy(image = null).toDomain()
        assertNull(asset.image)
    }

    @Test
    fun `Sparkline with very small values maps correctly`() {
        val asset = anAssetEntity(sparkline = "0.00000043,0.00000001").toDomain()
        assertEquals(listOf(0.00000043, 0.00000001), asset.sparkline)
    }

    @Test
    fun `Sparkline with negative values maps correctly`() {
        val asset = anAssetEntity(sparkline = "-1.0,2.0,-3.5").toDomain()
        assertEquals(listOf(-1.0, 2.0, -3.5), asset.sparkline)
    }
}
