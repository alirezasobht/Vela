package com.vela.data.source.local.mapper

import com.vela.data.source.local.model.AssetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssetEntityMapperTest {
    // ----- sparkline CSV -----

    @Test
    fun `sparkline CSV string is parsed to correct double list`() {
        val entity = assetEntity(sparkline = "1.0,2.0,3.0")

        val asset = entity.toDomain()

        assertEquals(listOf(1.0, 2.0, 3.0), asset.sparkline)
    }

    @Test
    fun `sparkline with malformed values skips invalid entries`() {
        val entity = assetEntity(sparkline = "1.0,abc,3.0,4,-1.0")

        val asset = entity.toDomain()

        assertEquals(listOf(1.0, 3.0, 4.0, -1.0), asset.sparkline)
    }

    @Test
    fun `null sparkline produces null domain sparkline`() {
        val asset = assetEntity(sparkline = null).toDomain()
        assertNull(asset.sparkline)
    }

    // ----- helpers -----

    private fun assetEntity(sparkline: String?) = AssetEntity(
        id = "bitcoin",
        symbol = "btc",
        name = "Bitcoin",
        image = null,
        currentPrice = null,
        priceChangePercent24h = null,
        marketCapRank = null,
        sparkline = sparkline
    )
}
