@file:Suppress("UnusedFlow")

package com.vela.data.repository

import app.cash.turbine.test
import com.vela.data.source.local.dao.HomeAssetDao
import com.vela.data.source.local.model.AssetEntity
import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.model.CoinDto
import com.vela.data.source.remote.model.SparklineDto
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AssetRepositoryImplTest {

    private val api: CoinGeckoApi = mockk()
    private val dao: HomeAssetDao = mockk()
    private val repository = AssetRepositoryImpl(api, dao)

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

    private fun anAssetEntity(id: String = "bitcoin") = AssetEntity(
        id = id,
        symbol = "btc",
        name = "Bitcoin",
        image = "https://example.com/btc.png",
        currentPrice = 67420.0,
        priceChangePercent24h = 2.4,
        marketCapRank = 1,
        sparkline = "1.0,2.0,3.0"
    )

    // ---- getTopAssets ----

    @Test
    fun `getTopAssets emits assets from database`() = runTest {
        every { dao.observeAll(any()) } returns flowOf(listOf(anAssetEntity()))

        val result = repository.getTopAssets().first()

        assertTrue(result is DataResult.Success)
        assertEquals(1, (result as DataResult.Success).data.size)
        assertEquals("bitcoin", result.data[0].id)
    }

    @Test
    fun `getTopAssets handles empty database state`() = runTest {
        every { dao.observeAll(any()) } returns flowOf(emptyList())

        val result = repository.getTopAssets().first()

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }

    @Test
    fun `getTopAssets applies limit parameter to database query`() = runTest {
        every { dao.observeAll(25) } returns flowOf(emptyList())

        repository.getTopAssets(limit = 25).first()

        verify { dao.observeAll(25) }
    }

    @Test
    fun `getTopAssets emits database updates in real time`() = runTest {

        val entityFlow = MutableSharedFlow<List<AssetEntity>>(extraBufferCapacity = 1)
        every { dao.observeAll(any()) } returns entityFlow

        repository.getTopAssets().test {

            entityFlow.emit(listOf(anAssetEntity("bitcoin")))
            awaitItem().let { emit1 ->
                assertTrue(emit1 is DataResult.Success)
                assertEquals(1, (emit1 as DataResult.Success).data.size)
                assertEquals("bitcoin", emit1.data[0].id)
            }

            entityFlow.emit(listOf(anAssetEntity("bitcoin"), anAssetEntity("ethereum")))
            awaitItem().let { emit2 ->
                assertTrue(emit2 is DataResult.Success)
                assertEquals(2, (emit2 as DataResult.Success).data.size)
                assertEquals("bitcoin", emit2.data[0].id)
                assertEquals("ethereum", emit2.data[1].id)
            }

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTopAssets mapping integrity from Entity to Domain`() = runTest {
        val entity = anAssetEntity()
        every { dao.observeAll(any()) } returns flowOf(listOf(entity))

        val asset = (repository.getTopAssets().first() as DataResult.Success).data[0]

        assertEquals(entity.id, asset.id)
        assertEquals(entity.symbol, asset.symbol)
        assertEquals(entity.name, asset.name)
        assertEquals(entity.image, asset.image)
        assertEquals(entity.currentPrice, asset.currentPrice)
        assertEquals(entity.priceChangePercent24h, asset.priceChangePercent24h)
        assertEquals(entity.marketCapRank, asset.marketCapRank)
        assertEquals(listOf(1.0, 2.0, 3.0), asset.sparkline)
    }

    // ---- refresh ----

    @Test
    fun `refresh successfully updates local database`() = runTest {
        coEvery { api.getMarkets(limit = any()) } returns listOf(aCoinDto())
        coEvery { dao.refresh(any()) } just runs

        val result = repository.fetchTopAssets()

        assertTrue(result is DataResult.Success)
        coVerify { dao.refresh(any()) }
    }

    @Test
    fun `refresh returns DataResult Success Unit on completion`() = runTest {
        coEvery { api.getMarkets(limit = any()) } returns listOf(aCoinDto())
        coEvery { dao.refresh(any()) } just runs

        val result = repository.fetchTopAssets()

        assertEquals(DataResult.Success(Unit), result)
    }

    @Test
    fun `refresh Network connectivity failure`() = runTest {
        coEvery { api.getMarkets(limit = any()) } throws IOException("No internet")

        val result = repository.fetchTopAssets()

        assertTrue(result is DataResult.Error)
        assertEquals(AppError.NoInternet, (result as DataResult.Error).appError)
    }

    @Test
    fun `refresh Generic exception handling`() = runTest {
        coEvery { api.getMarkets(limit = any()) } throws RuntimeException("Server crashed")

        val result = repository.fetchTopAssets()

        assertTrue(result is DataResult.Error)
        val error = (result as DataResult.Error).appError
        assertTrue(error is AppError.Unknown)
        assertEquals("Server crashed", (error as AppError.Unknown).message)
    }

    @Test
    fun `refresh Exception with null message handling`() = runTest {
        coEvery { api.getMarkets(limit = any()) } throws RuntimeException(null as String?)

        val result = repository.fetchTopAssets()

        assertTrue(result is DataResult.Error)
        val error = (result as DataResult.Error).appError
        assertTrue(error is AppError.Unknown)
        assertEquals("Something went wrong", (error as AppError.Unknown).message)
    }

    @Test
    fun `fetchTopAssets handles database transaction failure`() = runTest {
        coEvery { api.getMarkets(limit = any()) } returns listOf(aCoinDto())
        coEvery { dao.refresh(any()) } throws RuntimeException("DB error")

        val result = repository.fetchTopAssets()

        assertTrue(result is DataResult.Error)
        val error = (result as DataResult.Error).appError
        assertTrue(error is AppError.Unknown)
        assertEquals("DB error", (error as AppError.Unknown).message)
    }

    @Test
    fun `refresh Limit parameter integrity`() = runTest {
        coEvery { api.getMarkets(limit = 25) } returns emptyList()
        coEvery { dao.refresh(any()) } just runs

        repository.fetchTopAssets(limit = 25)

        coVerify { api.getMarkets(limit = 25) }
    }

    @Test
    fun `refresh handles zero or negative limit values`() = runTest {
        coEvery { api.getMarkets(limit = 0) } returns emptyList()
        coEvery { dao.refresh(any()) } just runs

        val result = repository.fetchTopAssets(limit = 0)

        coVerify { api.getMarkets(limit = 0) }
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `refresh Large limit value`() = runTest {
        coEvery { api.getMarkets(limit = Int.MAX_VALUE) } returns emptyList()
        coEvery { dao.refresh(any()) } just runs

        val result = repository.fetchTopAssets(limit = Int.MAX_VALUE)

        coVerify { api.getMarkets(limit = Int.MAX_VALUE) }
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `refresh Nullable fields in CoinDto are handled`() = runTest {
        val dto = aCoinDto().copy(currentPrice = null, priceChangePercent24h = null, marketCapRank = null)
        coEvery { api.getMarkets(limit = any()) } returns listOf(dto)
        coEvery { dao.refresh(any()) } just runs

        val result = repository.fetchTopAssets()

        assertTrue(result is DataResult.Success)
        coVerify { dao.refresh(any()) }
    }

    @Test
    fun `refresh mapping integrity from DTO to Entity`() = runTest {
        val dto = aCoinDto()
        coEvery { api.getMarkets(limit = any()) } returns listOf(dto)
        val entitiesSlot = slot<List<AssetEntity>>()
        coEvery { dao.refresh(capture(entitiesSlot)) } just runs

        repository.fetchTopAssets()

        val entity = entitiesSlot.captured[0]
        assertEquals(dto.id, entity.id)
        assertEquals(dto.symbol, entity.symbol)
        assertEquals(dto.name, entity.name)
        assertEquals(dto.image, entity.image)
        assertEquals(dto.currentPrice, entity.currentPrice)
        assertEquals(dto.priceChangePercent24h, entity.priceChangePercent24h)
        assertEquals(dto.marketCapRank, entity.marketCapRank)
    }
}