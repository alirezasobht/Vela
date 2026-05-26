package com.vela.data.source.remote.mapper

import com.vela.data.source.remote.model.CoinDto
import com.vela.data.source.remote.model.SparklineDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoinDtoMapperKtTest {

    private fun aCoinDto(
        id: String = "bitcoin",
        symbol: String = "btc",
        name: String = "Bitcoin",
        image: String? = "https://example.com/btc.png",
        currentPrice: Double? = 67420.0,
        priceChangePercent24h: Double? = 2.4,
        marketCapRank: Int? = 1,
        sparkline: SparklineDto? = SparklineDto(price = listOf(65000.0, 66000.0, 67420.0))
    ) = CoinDto(
        id = id,
        symbol = symbol,
        name = name,
        image = image,
        currentPrice = currentPrice,
        priceChangePercent24h = priceChangePercent24h,
        marketCapRank = marketCapRank,
        sparkline = sparkline
    )

    @Test
    fun `Successful full mapping`() {
        val dto = aCoinDto()
        val asset = dto.toDomain()

        assertEquals(dto.id, asset.id)
        assertEquals(dto.symbol, asset.symbol)
        assertEquals(dto.name, asset.name)
        assertEquals(dto.image, asset.image)
        assertEquals(dto.currentPrice, asset.currentPrice)
        assertEquals(dto.priceChangePercent24h, asset.priceChangePercent24h)
        assertEquals(dto.marketCapRank, asset.marketCapRank)
        assertEquals(dto.sparkline?.price, asset.sparkline)
    }

    @Test
    fun `Sparkline object is null`() {
        val asset = aCoinDto(sparkline = null).toDomain()
        assertNull(asset.sparkline)
    }

    @Test
    fun `Sparkline price property is null`() {
        // SparklineDto.price is non-nullable in the current model.
        // If sparkline object is present, price is always a list.
        // This test documents that behavior — sparkline with empty list maps correctly.
        val asset = aCoinDto(sparkline = SparklineDto(price = emptyList())).toDomain()
        assertEquals(emptyList<Double>(), asset.sparkline)
    }

    @Test
    fun `Empty strings for metadata fields`() {
        val asset = aCoinDto(id = "", symbol = "", name = "", image = "").toDomain()
        assertEquals("", asset.id)
        assertEquals("", asset.symbol)
        assertEquals("", asset.name)
        assertEquals("", asset.image)
    }

    @Test
    fun `Zero and negative numerical values`() {
        val asset = aCoinDto(currentPrice = 0.0, priceChangePercent24h = -5.0).toDomain()
        assertEquals(0.0, asset.currentPrice)
        assertEquals(-5.0, asset.priceChangePercent24h)
    }

    @Test
    fun `Large integer marketCapRank mapping`() {
        val asset = aCoinDto(marketCapRank = Int.MAX_VALUE).toDomain()
        assertEquals(Int.MAX_VALUE, asset.marketCapRank)
    }

    @Test
    fun `Extreme double precision values`() {
        val asset = aCoinDto(currentPrice = Double.MAX_VALUE).toDomain()
        assertEquals(Double.MAX_VALUE, asset.currentPrice)

        val assetMin = aCoinDto(currentPrice = Double.MIN_VALUE).toDomain()
        assertEquals(Double.MIN_VALUE, assetMin.currentPrice)
    }

    @Test
    fun `Sparkline price list with data`() {
        val prices = listOf(100.0, 200.0, 300.0, 400.0)
        val asset = aCoinDto(sparkline = SparklineDto(price = prices)).toDomain()
        assertEquals(prices, asset.sparkline)
    }
}