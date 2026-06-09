package com.vela.domain.usecase

import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.SearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchAssetsUseCaseTest {

    private val repository: SearchRepository = mockk()
    private val useCase = SearchAssetsUseCase(repository)

    // ----- search error -----

    @Test
    fun `search error propagates without calling getPricesByIds`() = runTest {
        val error = DataResult.Error(AppError.NoInternet)
        coEvery { repository.search(any()) } returns error

        val result = useCase("bitcoin")

        assertEquals(error, result)
        coVerify(exactly = 0) { repository.getPricesByIds(any()) }
    }

    // ----- search success + price fetch -----

    @Test
    fun `search success and price fetch success returns enriched list`() = runTest {
        val searchAssets = listOf(asset("bitcoin"), asset("ethereum"))
        val enrichedAssets = listOf(asset("bitcoin", price = 30_000.0), asset("ethereum", price = 2_000.0))
        coEvery { repository.search(any()) } returns DataResult.Success(searchAssets)
        coEvery { repository.getPricesByIds(any()) } returns DataResult.Success(enrichedAssets)

        val result = useCase("crypto")

        assertEquals(DataResult.Success(enrichedAssets), result)
    }

    @Test
    fun `search success and price fetch error falls back to unenriched assets`() = runTest {
        val searchAssets = listOf(asset("bitcoin"), asset("ethereum"))
        coEvery { repository.search(any()) } returns DataResult.Success(searchAssets)
        coEvery { repository.getPricesByIds(any()) } returns DataResult.Error(AppError.NoInternet)

        val result = useCase("crypto")

        assertEquals(DataResult.Success(searchAssets), result)
    }

    @Test
    fun `search success with empty list skips price fetch and returns empty`() = runTest {
        coEvery { repository.search(any()) } returns DataResult.Success(emptyList())

        val result = useCase("unknown")

        assertEquals(DataResult.Success(emptyList<Asset>()), result)
        coVerify(exactly = 0) { repository.getPricesByIds(any()) }
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