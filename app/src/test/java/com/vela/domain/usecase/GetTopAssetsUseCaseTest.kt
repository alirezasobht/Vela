package com.vela.domain.usecase

import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("UnusedFlow")
class GetTopAssetsUseCaseTest {

    private val repository: AssetRepository = mockk()
    private val useCase = GetTopAssetsUseCase(repository)

    private fun anAsset(id: String = "bitcoin") = Asset(
        id = id, symbol = "btc", name = "Bitcoin",
        image = null, currentPrice = 67420.0,
        priceChangePercent24h = 2.4, marketCapRank = 1,
        sparkline = listOf(1.0, 2.0, 3.0)
    )

    @Test
    fun `invoke with default limit value`() = runTest {
        every { repository.getTopAssets(50) } returns flowOf(DataResult.Success(emptyList()))

        useCase()

        verify { repository.getTopAssets(50) }
    }

    @Test
    fun `invoke with custom positive limit`() = runTest {
        every { repository.getTopAssets(25) } returns flowOf(DataResult.Success(emptyList()))

        useCase(25)

        verify { repository.getTopAssets(25) }
    }

    @Test
    fun `invoke with zero limit boundary`() = runTest {
        every { repository.getTopAssets(0) } returns flowOf(DataResult.Success(emptyList()))

        useCase(0)

        verify { repository.getTopAssets(0) }
    }

    @Test
    fun `invoke with negative limit value`() = runTest {
        every { repository.getTopAssets(-1) } returns flowOf(DataResult.Success(emptyList()))

        useCase(-1)

        verify { repository.getTopAssets(-1) }
    }

    @Test
    fun `invoke with maximum integer limit`() = runTest {
        every { repository.getTopAssets(Int.MAX_VALUE) } returns flowOf(DataResult.Success(emptyList()))

        useCase(Int.MAX_VALUE)

        verify { repository.getTopAssets(Int.MAX_VALUE) }
    }

    @Test
    fun `successful repository emission with data`() = runTest {
        val assets = listOf(anAsset())
        every { repository.getTopAssets(any()) } returns flowOf(DataResult.Success(assets))

        val results = useCase().toList()

        assertEquals(1, results.size)
        assertEquals(true, results[0] is DataResult.Success)
        assertEquals(assets, (results[0] as DataResult.Success).data)
    }

    @Test
    fun `successful repository emission with empty list`() = runTest {
        every { repository.getTopAssets(any()) } returns flowOf(DataResult.Success(emptyList()))

        val results = useCase().toList()

        assertEquals(1, results.size)
        assertEquals(true, results[0] is DataResult.Success)
        assertEquals(true, (results[0] as DataResult.Success).data.isEmpty())
    }

    @Test
    fun `repository emission with failure result`() = runTest {
        every { repository.getTopAssets(any()) } returns flowOf(DataResult.Error(AppError.NoInternet))

        val results = useCase().toList()

        assertEquals(1, results.size)
        assertEquals(true, results[0] is DataResult.Error)
        assertEquals(AppError.NoInternet, (results[0] as DataResult.Error).appError)
    }

    @Test
    fun `repository throws immediate exception`() = runTest {
        every { repository.getTopAssets(any()) } throws RuntimeException("Unexpected")

        var exception: RuntimeException? = null
        try {
            useCase().toList()
        } catch (e: RuntimeException) {
            exception = e
        }

        assertEquals("Unexpected", exception?.message)
    }

    @Test
    fun `flow emission sequence integrity`() = runTest {
        val firstBatch = listOf(anAsset("bitcoin"))
        val secondBatch = listOf(anAsset("bitcoin"), anAsset("ethereum"))
        every { repository.getTopAssets(any()) } returns flowOf(
            DataResult.Success(firstBatch),
            DataResult.Success(secondBatch)
        )

        val results = useCase().toList()

        assertEquals(2, results.size)
        assertEquals(1, (results[0] as DataResult.Success).data.size)
        assertEquals(2, (results[1] as DataResult.Success).data.size)
    }
}