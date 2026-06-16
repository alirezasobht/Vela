package com.vela.ui.screens.watchlist

import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.domain.usecase.GetWatchlistAssetsUseCase
import com.vela.domain.usecase.IsWatchlistedUseCase
import com.vela.domain.usecase.ToggleWatchlistUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.pricepolling.PricePollingConfig
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.base.watchlist.WatchlistControllerImpl
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val getWatchlistAssets: GetWatchlistAssetsUseCase = mockk()
    private val getPrices: GetPricesOnlyUseCase = mockk(relaxed = true)
    private val assetPreviewCache: AssetPreviewCache = mockk(relaxed = true)
    private val config: PricePollingConfig = mockk { every { refreshDelaySeconds } returns 60L }
    private val isWatchlistedUseCase: IsWatchlistedUseCase = mockk(relaxed = true)
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase = mockk(relaxed = true)

    private fun asset(id: String) =
        Asset(
            id = id,
            symbol = id,
            name = id,
            image = null,
            currentPrice = 100.0,
            priceChangePercent24h = 1.0,
            marketCapRank = 1,
            sparkline = null
        )

    private fun createViewModel() =
        WatchlistViewModel(
            getWatchlistAssets = getWatchlistAssets,
            getPrices = getPrices,
            assetPreviewCache = assetPreviewCache,
            pricePolling = PricePollingController(config),
            watchlistController = WatchlistControllerImpl(isWatchlistedUseCase, toggleWatchlistUseCase)
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        every { getWatchlistAssets() } returns MutableSharedFlow()

        val vm = createViewModel()

        assertEquals(true, vm.uiState.value is WatchlistUiState.Loading)
    }

    @Test
    fun `empty watchlist results in Empty state`() =
        runTest {
            every { getWatchlistAssets() } returns flowOf(DataResult.Success(emptyList()))

            val vm = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, vm.uiState.value is WatchlistUiState.Empty)
        }

    @Test
    fun `non-empty watchlist results in Success with mapped models`() =
        runTest {
            val assets = listOf(asset("bitcoin"), asset("ethereum"))
            every { getWatchlistAssets() } returns flowOf(DataResult.Success(assets))

            val vm = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            val state = vm.uiState.value as WatchlistUiState.Success
            assertEquals(2, state.assets.size)
            assertEquals("bitcoin", state.assets[0].id)
            assertEquals("ethereum", state.assets[1].id)
        }

    @Test
    fun `assets are cached in AssetPreviewCache`() =
        runTest {
            val assets = listOf(asset("bitcoin"), asset("ethereum"))
            every { getWatchlistAssets() } returns flowOf(DataResult.Success(assets))

            createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            coVerify { assetPreviewCache.put(asset("bitcoin")) }
            coVerify { assetPreviewCache.put(asset("ethereum")) }
        }

    @Test
    fun `error results in Empty state`() =
        runTest {
            every { getWatchlistAssets() } returns flowOf(DataResult.Error(AppError.NoInternet))

            val vm = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, vm.uiState.value is WatchlistUiState.Empty)
        }

    @Test
    fun `getIds returns ids from Success state, empty otherwise`() =
        runTest {
            val assets = listOf(asset("bitcoin"), asset("ethereum"))
            every { getWatchlistAssets() } returns flowOf(DataResult.Success(assets))

            val vm = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(listOf("bitcoin", "ethereum"), vm.getIds())
        }

    @Test
    fun `getIds returns empty list when state is Empty`() =
        runTest {
            every { getWatchlistAssets() } returns flowOf(DataResult.Success(emptyList()))

            val vm = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(emptyList<String>(), vm.getIds())
        }

    @Test
    fun `onPriceError sets nonBlockingError only when state is Success`() =
        runTest {
            val assets = listOf(asset("bitcoin"))
            every { getWatchlistAssets() } returns flowOf(DataResult.Success(assets))

            val vm = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            vm.onPriceError(AppError.NoInternet)

            val state = vm.uiState.value as WatchlistUiState.Success
            assertEquals(AppError.NoInternet, state.nonBlockingError)
        }

    @Test
    fun `onPriceError is no-op when state is Empty`() =
        runTest {
            every { getWatchlistAssets() } returns flowOf(DataResult.Success(emptyList()))

            val vm = createViewModel()
            dispatcher.scheduler.advanceUntilIdle()

            vm.onPriceError(AppError.NoInternet)

            assertEquals(true, vm.uiState.value is WatchlistUiState.Empty)
        }
}
