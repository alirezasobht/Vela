package com.vela.domain.usecase

import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTopAssetsUseCaseTest {

    private val repository: AssetRepository = mockk()
    private val useCase = GetTopAssetsUseCase(repository)

    private fun anAsset(id: String = "bitcoin") = Asset(
        id = id,
        symbol = "btc",
        name = "Bitcoin",
        image = null,
        currentPrice = 67420.0,
        priceChangePercent24h = 2.4,
        marketCapRank = 1,
        sparkline = listOf(1.0, 2.0, 3.0)
    )

    @Test
    fun `successful asset retrieval`() = runTest {
        val assets = listOf(anAsset())
        coEvery { repository.getTopAssets(any()) } returns DataResult.Success(assets)

        val result = useCase()

        assertTrue(result is DataResult.Success)
        assertEquals(assets, (result as DataResult.Success).data)
    }

    @Test
    fun `default limit parameter usage`() = runTest {
        coEvery { repository.getTopAssets(50) } returns DataResult.Success(emptyList())

        useCase()

        coVerify { repository.getTopAssets(50) }
    }

    @Test
    fun `zero limit boundary check`() = runTest {

        val result = useCase(limit = 0)

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
        coVerify(exactly = 0) { repository.getTopAssets(any()) }
    }

    @Test
    fun `negative limit handling`() = runTest {

        val result = useCase(limit = -1)

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
        coVerify(exactly = 0) { repository.getTopAssets(any()) }
    }

    @Test
    fun `maximum integer limit check`() = runTest {
        coEvery { repository.getTopAssets(Int.MAX_VALUE) } returns DataResult.Success(emptyList())

        useCase(limit = Int.MAX_VALUE)

        coVerify { repository.getTopAssets(Int.MAX_VALUE) }
    }

    @Test
    fun `repository empty state handling`() = runTest {
        coEvery { repository.getTopAssets(any()) } returns DataResult.Success(emptyList())

        val result = useCase()

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }

    @Test
    fun `repository error propagation`() = runTest {
        val error = DataResult.Error(AppError.NoInternet)
        coEvery { repository.getTopAssets(any()) } returns error

        val result = useCase()

        assertTrue(result is DataResult.Error)
        assertSame(AppError.NoInternet, (result as DataResult.Error).exception)
    }

    @Test
    fun `repository exception safety`() = runTest {
        val error = DataResult.Error(AppError.Unknown("Unexpected error"))
        coEvery { repository.getTopAssets(any()) } returns error

        val result = useCase()

        assertTrue(result is DataResult.Error)
        assertTrue((result as DataResult.Error).exception is AppError.Unknown)
    }

    @Test
    fun `large data set mapping performance`() = runTest {
        val largeList = (1..1000).map { anAsset(id = "asset_$it") }
        coEvery { repository.getTopAssets(1000) } returns DataResult.Success(largeList)

        val result = useCase(limit = 1000)

        assertTrue(result is DataResult.Success)
        assertEquals(1000, (result as DataResult.Success).data.size)
    }
}