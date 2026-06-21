package com.vela.domain.usecase

import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import com.vela.domain.repository.WatchlistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWatchlistAssetsUseCaseTest {
    private val watchlistRepository: WatchlistRepository = mockk()
    private val assetRepository: AssetRepository = mockk()
    private val useCase = GetWatchlistAssetsUseCase(watchlistRepository, assetRepository)

    private fun asset(id: String) = Asset(
        id = id,
        symbol = id,
        name = id,
        image = null,
        currentPrice = null,
        priceChangePercent24h = null,
        marketCapRank = null,
        sparkline = null
    )

    @Test
    fun `empty watchlist emits empty success without calling getAssetsByIds`() = runTest {
        every { watchlistRepository.observeWatchlist() } returns MutableStateFlow(emptyList())

        val result = useCase().first()

        assertEquals(DataResult.Success(emptyList<Asset>()), result)
        coVerify(exactly = 0) { assetRepository.getAssetsByIds(any()) }
    }

    @Test
    fun `non-empty watchlist calls getAssetsByIds with correct ids`() = runTest {
        val ids = listOf("bitcoin", "ethereum")
        val assets = ids.map { asset(it) }
        every { watchlistRepository.observeWatchlist() } returns MutableStateFlow(ids)
        coEvery { assetRepository.getAssetsByIds(ids) } returns DataResult.Success(assets)

        val result = useCase().first()

        assertEquals(DataResult.Success(assets), result)
        coVerify { assetRepository.getAssetsByIds(ids) }
    }

    @Test
    fun `getAssetsByIds error propagates`() = runTest {
        val ids = listOf("bitcoin")
        every { watchlistRepository.observeWatchlist() } returns MutableStateFlow(ids)
        coEvery { assetRepository.getAssetsByIds(ids) } returns DataResult.Error(AppError.NoInternet)

        val result = useCase().first()

        assertEquals(DataResult.Error(AppError.NoInternet), result)
    }

    @Test
    fun `watchlist change re-fetches with new ids`() = runTest {
        val idsFlow = MutableStateFlow<List<String>>(emptyList())
        every { watchlistRepository.observeWatchlist() } returns idsFlow
        coEvery { assetRepository.getAssetsByIds(listOf("bitcoin")) } returns
            DataResult.Success(listOf(asset("bitcoin")))
        coEvery { assetRepository.getAssetsByIds(listOf("bitcoin", "ethereum")) } returns
            DataResult.Success(listOf(asset("bitcoin"), asset("ethereum")))

        idsFlow.value = listOf("bitcoin")
        val first = useCase().first()
        assertEquals(1, (first as DataResult.Success).data.size)

        idsFlow.value = listOf("bitcoin", "ethereum")
        val second = useCase().first()
        assertEquals(2, (second as DataResult.Success).data.size)
    }
}
