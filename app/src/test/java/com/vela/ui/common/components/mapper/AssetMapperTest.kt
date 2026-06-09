package com.vela.ui.common.components.mapper

import com.vela.domain.model.SimplePrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetMapperTest {

    // ----- formatPrice -----

    @Test
    fun `formatPrice - sub-cent trims trailing zeros but keeps significant digits`() {
        assertEquals("$0.00001234", formatPrice(0.00001234))
    }

    @Test
    fun `formatPrice - value below threshold uses 4 decimal floor`() {
        assertEquals("$0.0000", formatPrice(0.000001))
    }

    @Test
    fun `formatPrice - exactly 1 uses two decimal format`() {
        assertEquals("$1.00", formatPrice(1.0))
    }

    @Test
    fun `formatPrice - exactly 1000 uses comma format with no decimals`() {
        assertEquals("$1,000", formatPrice(1000.0))
    }

    @Test
    fun `formatPrice - 999 stays in decimal format`() {
        assertEquals("$999.00", formatPrice(999.0))
    }

    // ----- formatSignedPrice -----

    @Test
    fun `formatSignedPrice - negative value produces minus prefix not plus-minus`() {
        val result = formatSignedPrice(-500.0)
        assertTrue(result.startsWith("-"))
        assertTrue(!result.startsWith("+-"))
    }

    @Test
    fun `formatSignedPrice - positive value produces plus prefix`() {
        assertTrue(formatSignedPrice(100.0).startsWith("+"))
    }

    // ----- formatLargeNumber -----

    @Test
    fun `formatLargeNumber - exactly 1 million uses M suffix`() {
        assertEquals("1.00M", formatLargeNumber(1_000_000.0))
    }

    @Test
    fun `formatLargeNumber - exactly 1 billion uses B suffix`() {
        assertEquals("1.00B", formatLargeNumber(1_000_000_000.0))
    }

    @Test
    fun `formatLargeNumber - exactly 1 trillion uses T suffix`() {
        assertEquals("1.00T", formatLargeNumber(1_000_000_000_000.0))
    }

    // ----- formatSupply -----

    @Test
    fun `formatSupply - appends symbol correctly`() {
        assertEquals("19.00M BTC", formatSupply(19_000_000.0, "BTC"))
    }

    // ----- formatChange -----

    @Test
    fun `formatChange - zero produces positive sign`() {
        assertEquals("+0.00%", formatChange(0.0))
    }

    // ----- SimplePrice.priceChange24hAbsolute -----

    @Test
    fun `priceChange24hAbsolute - derived correctly from price and percent`() {
        val price = SimplePrice(price = 100.0, priceChange = 2.5, marketCap = null, totalVolume = null)
        assertEquals(2.5, price.priceChange24hAbsolute!!,  0.0001)
    }

    @Test
    fun `priceChange24hAbsolute - null when price is null`() {
        val price = SimplePrice(price = null, priceChange = 2.5, marketCap = null, totalVolume = null)
        assertNull(price.priceChange24hAbsolute)
    }

    @Test
    fun `priceChange24hAbsolute - null when priceChange is null`() {
        val price = SimplePrice(price = 100.0, priceChange = null, marketCap = null, totalVolume = null)
        assertNull(price.priceChange24hAbsolute)
    }

    // ----- SimplePrice.toUiModel - isPositive -----

    @Test
    fun `toUiModel - isPositive is true when priceChange is exactly 0`() {
        val model = SimplePrice(price = 100.0, priceChange = 0.0, marketCap = null, totalVolume = null).toUiModel()
        assertTrue(model.isPositive)
    }
}