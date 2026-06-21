package com.vela.ui.screens.detail.state

import com.vela.domain.model.CoinDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class CoinDetailMapperTest {
    // ----- priceChange24h sign formatting -----

    @Test
    fun `priceChange24h - positive value produces plus prefix`() {
        val model = coinDetail(priceChange24h = 500.0).toUiModel()
        assertEquals(true, model.priceChange24h.startsWith("+"))
    }

    @Test
    fun `priceChange24h - negative value produces minus prefix not double-negative`() {
        val model = coinDetail(priceChange24h = -500.0).toUiModel()
        assertEquals(true, model.priceChange24h.startsWith("-"))
        assertEquals(false, model.priceChange24h.startsWith("--"))
    }

    @Test
    fun `priceChange24h - null produces em dash`() {
        val model = coinDetail(priceChange24h = null).toUiModel()
        assertEquals("\u2014", model.priceChange24h)
    }

    // ----- isPositive -----

    @Test
    fun `isPositive - true when priceChangePercent24h is exactly 0`() {
        val model = coinDetail(priceChangePercent24h = 0.0).toUiModel()
        assertEquals(true, model.isPositive)
    }

    @Test
    fun `isPositive - false when priceChangePercent24h is negative`() {
        val model = coinDetail(priceChangePercent24h = -1.0).toUiModel()
        assertEquals(false, model.isPositive)
    }

    // ----- null fields -----

    @Test
    fun `all nullable fields null produce em dash not empty string or crash`() {
        val model = coinDetail(
            currentPrice = null,
            priceChange24h = null,
            priceChangePercent24h = null,
            marketCap = null,
            totalVolume = null,
            circulatingSupply = null,
            ath = null,
            atl = null
        ).toUiModel()
        val dash = "\u2014"
        assertEquals(dash, model.currentPrice)
        assertEquals(dash, model.priceChange24h)
        assertEquals(dash, model.priceChangePercent24h)
        assertEquals(dash, model.marketCap)
        assertEquals(dash, model.totalVolume)
        assertEquals(dash, model.circulatingSupply)
        assertEquals(dash, model.ath)
        assertEquals(dash, model.atl)
    }

    // ----- symbol uppercase -----

    @Test
    fun `symbol is always uppercased`() {
        val model = coinDetail(symbol = "btc").toUiModel()
        assertEquals("BTC", model.symbol)
    }

    // ----- helpers -----

    private fun coinDetail(
        symbol: String = "btc",
        currentPrice: Double? = 30_000.0,
        priceChange24h: Double? = 500.0,
        priceChangePercent24h: Double? = 1.5,
        marketCap: Double? = 600_000_000_000.0,
        totalVolume: Double? = 20_000_000_000.0,
        circulatingSupply: Double? = 19_000_000.0,
        ath: Double? = 69_000.0,
        atl: Double? = 67.81
    ) = CoinDetail(
        id = "bitcoin",
        symbol = symbol,
        name = "Bitcoin",
        image = null,
        currentPrice = currentPrice,
        priceChange24h = priceChange24h,
        priceChangePercent24h = priceChangePercent24h,
        marketCap = marketCap,
        totalVolume = totalVolume,
        circulatingSupply = circulatingSupply,
        ath = ath,
        atl = atl,
        marketCapRank = 1
    )
}
