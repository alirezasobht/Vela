package com.vela.ui.base

import com.vela.domain.model.Asset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssetPreviewCacheTest {

    private val holder = AssetPreviewCache()

    // ----- put(list) + get -----

    @Test
    fun `put list then get returns correct asset`() {
        val assets = listOf(asset("bitcoin"), asset("ethereum"))
        holder.put(assets)

        assertEquals(assets[0], holder.get("bitcoin"))
        assertEquals(assets[1], holder.get("ethereum"))
    }

    // ----- put(asset) overwrite -----

    @Test
    fun `put asset overwrites existing entry with same id`() {
        holder.put(asset("bitcoin", price = 30_000.0))
        holder.put(asset("bitcoin", price = 35_000.0))

        assertEquals(35_000.0, holder.get("bitcoin")?.currentPrice)
    }

    // ----- unknown id -----

    @Test
    fun `get for unknown id returns null`() {
        assertNull(holder.get("unknown"))
    }

    // ----- helpers -----

    private fun asset(id: String, price: Double? = null) = Asset(
        id = id,
        symbol = id,
        name = id,
        image = null,
        currentPrice = price,
        priceChangePercent24h = null,
        marketCapRank = null,
        sparkline = null
    )
}