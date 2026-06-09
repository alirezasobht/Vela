package com.vela.ui.common.components.mapper

import com.vela.domain.model.SimplePrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetMapperTest {

    // ----- formatPrice -----

    private fun assertPriceFormat(input: Double, expected: String) {
        assertEquals("formatPrice($input)", expected, formatPrice(input))
    }

    @Test
    fun `formatPrice - large trillion value`() =
        assertPriceFormat(1_000_000_000_000.00, "$1,000,000,000,000")

    @Test
    fun `formatPrice - rounds down at 1000`() =
        assertPriceFormat(1_000.001, "$1,000")

    @Test
    fun `formatPrice - rounds cents at 1001`() =
        assertPriceFormat(1_001.01, "$1,001")

    @Test
    fun `formatPrice - whole number fifty thousand`() =
        assertPriceFormat(50_000.0, "$50,000")

    @Test
    fun `formatPrice - whole number one thousand`() =
        assertPriceFormat(1_000.0, "$1,000")

    @Test
    fun `formatPrice - cents below one thousand`() =
        assertPriceFormat(999.99, "$999.99")

    @Test
    fun `formatPrice - padding zero for whole number small price`() =
        assertPriceFormat(15.0, "$15.00")

    @Test
    fun `formatPrice - padding zero for single digit cents`() =
        assertPriceFormat(15.1, "$15.10")

    @Test
    fun `formatPrice - rounding small cents`() =
        assertPriceFormat(15.003, "$15.00")

    @Test
    fun `formatPrice - leading zero cents 0-50`() =
        assertPriceFormat(0.5, "$0.50")

    @Test
    fun `formatPrice - leading zero cents 0-10`() =
        assertPriceFormat(0.1, "$0.10")

    @Test
    fun `formatPrice - high precision sub-cent 8 digits`() =
        assertPriceFormat(0.00001234, "$0.00001234")

    @Test
    fun `formatPrice - high precision sub-cent 7 digits`() =
        assertPriceFormat(0.0000123, "$0.0000123")

    @Test
    fun `formatPrice - high precision sub-cent 6 digits`() =
        assertPriceFormat(0.000001, "$0.000001")

    // ----- formatSignedPrice -----

    @Test
    fun `formatSignedPrice - negative value produces minus prefix`() {
        val result = formatSignedPrice(-500.0)
        assertTrue(result.startsWith("-"))
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
    fun `formatChange - zero produces has no sign`() {
        assertEquals("0.00%", formatChange(0.0))
    }

    @Test
    fun `formatChange - positive value produces plus sign`() {
        assertEquals("+123.46%", formatChange(123.456))
    }

    @Test
    fun `formatChange - negative value produces minus sign`() {
        assertEquals("-12.34%", formatChange(-12.343))
    }

    // ----- SimplePrice.priceChange24hAbsolute -----

    @Test
    fun `priceChange24hAbsolute - derived correctly from price and percent`() {
        val price = SimplePrice(price = 100.0, priceChange = 2.5, marketCap = null, totalVolume = null)
        assertEquals(2.5, price.priceChange24hAbsolute!!, 0.0001)
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