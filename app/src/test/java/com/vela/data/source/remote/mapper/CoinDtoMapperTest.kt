package com.vela.data.source.remote.mapper

import com.vela.data.source.remote.model.CoinDto
import com.vela.data.source.remote.model.SparklineDto
import com.vela.domain.model.OhlcPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoinDtoMapperTest {
    // ----- CoinDto.toEntity - sparkline -----

    @Test
    fun `toEntity - sparkline longer than 14 is truncated to last 14`() {
        val prices = (1..20).map { it.toDouble() }
        val dto = coinDto(sparkline = SparklineDto(prices))

        val entity = dto.toEntity()

        val expected = (7..20).joinToString(",") { it.toDouble().toString() }
        assertEquals(expected, entity.sparkline)
    }

    @Test
    fun `toEntity - null sparkline produces null entity sparkline`() {
        val entity = coinDto(sparkline = null).toEntity()
        assertNull(entity.sparkline)
    }

    // ----- List<Double>.toOhlcPoint -----

    @Test
    fun `toOhlcPoint - maps indices correctly`() {
        val raw = listOf(1_700_000_000_000.0, 30_000.0, 31_000.0, 29_000.0, 30_500.0)

        val point = raw.toOhlcPoint()

        assertEquals(
            OhlcPoint(
                timestamp = 1_700_000_000_000L,
                open = 30_000.0,
                high = 31_000.0,
                low = 29_000.0,
                close = 30_500.0
            ),
            point
        )
    }

    // ----- helpers -----

    private fun coinDto(sparkline: SparklineDto?) =
        CoinDto(
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
