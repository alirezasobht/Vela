package com.vela.data.repository

import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.model.CoinDto
import com.vela.data.source.remote.model.SparklineDto
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AssetRepositoryImplTest {

    private val api: CoinGeckoApi = mockk()
    private val repository = AssetRepositoryImpl(api)

    private fun aCoinDto(id: String = "bitcoin") = CoinDto(
        id = id,
        symbol = "btc",
        name = "Bitcoin",
        image = "https://example.com/btc.png",
        currentPrice = 67420.0,
        priceChangePercent24h = 2.4,
        marketCapRank = 1,
        sparkline = SparklineDto(price = listOf(1.0, 2.0, 3.0))
    )

    @Test
    fun `Successful retrieval of assets`() = runTest {
        val dtos = listOf(aCoinDto())
        coEvery { api.getMarkets(limit = any()) } returns dtos

        val result = repository.getTopAssets()

        assertTrue(result is DataResult.Success)
        val assets = (result as DataResult.Success).data
        assertEquals(1, assets.size)
        assertEquals("bitcoin", assets[0].id)
        assertEquals("btc", assets[0].symbol)
        assertEquals(67420.0, assets[0].currentPrice!!, 0.001)
    }

    @Test
    fun `Empty list from API`() = runTest {
        coEvery { api.getMarkets(limit = any()) } returns emptyList()

        val result = repository.getTopAssets()

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }

    @Test
    fun `Network connectivity failure`() = runTest {
        coEvery { api.getMarkets(limit = any()) } throws IOException("No internet")

        val result = repository.getTopAssets()

        assertTrue(result is DataResult.Error)
        assertEquals(AppError.NoInternet, (result as DataResult.Error).appError)
    }

    @Test
    fun `Generic exception handling`() = runTest {
        coEvery { api.getMarkets(limit = any()) } throws RuntimeException("Server crashed")

        val result = repository.getTopAssets()

        assertTrue(result is DataResult.Error)
        val exception = (result as DataResult.Error).appError
        assertTrue(exception is AppError.Unknown)
        assertEquals("Server crashed", (exception as AppError.Unknown).message)
    }

    @Test
    fun `Exception with null message handling`() = runTest {
        coEvery { api.getMarkets(limit = any()) } throws RuntimeException(null as String?)

        val result = repository.getTopAssets()

        assertTrue(result is DataResult.Error)
        val exception = (result as DataResult.Error).appError
        assertTrue(exception is AppError.Unknown)
        assertEquals("Something went wrong", (exception as AppError.Unknown).message)
    }

    @Test
    fun `Limit parameter integrity`() = runTest {
        coEvery { api.getMarkets(limit = 25) } returns emptyList()

        repository.getTopAssets(limit = 25)

        coVerify { api.getMarkets(limit = 25) }
    }

    @Test
    fun `Mapping logic verification`() = runTest {
        val dtos = listOf(aCoinDto("bitcoin"), aCoinDto("ethereum"), aCoinDto("solana"))
        coEvery { api.getMarkets(limit = any()) } returns dtos

        val result = repository.getTopAssets()

        assertTrue(result is DataResult.Success)
        val assets = (result as DataResult.Success).data
        assertEquals(3, assets.size)
        assertEquals("bitcoin", assets[0].id)
        assertEquals("ethereum", assets[1].id)
        assertEquals("solana", assets[2].id)
    }

    @Test
    fun `Large limit value test`() = runTest {
        coEvery { api.getMarkets(limit = Int.MAX_VALUE) } returns emptyList()

        val result = repository.getTopAssets(limit = Int.MAX_VALUE)

        coVerify { api.getMarkets(limit = Int.MAX_VALUE) }
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `Nullable fields in CoinDto are handled`() = runTest {
        val dto = aCoinDto().copy(currentPrice = null, priceChangePercent24h = null, marketCapRank = null)
        coEvery { api.getMarkets(limit = any()) } returns listOf(dto)

        val result = repository.getTopAssets()

        assertTrue(result is DataResult.Success)
        val asset = (result as DataResult.Success).data[0]
        assertEquals(null, asset.currentPrice)
        assertEquals(null, asset.priceChangePercent24h)
        assertEquals(null, asset.marketCapRank)
    }
}