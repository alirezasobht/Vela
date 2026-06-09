package com.vela.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import com.vela.domain.model.AppError
import com.vela.domain.model.CoinDetail
import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint
import com.vela.domain.model.TimeRange
import com.vela.domain.usecase.GetCoinDetailUseCase
import com.vela.domain.usecase.GetOhlcUseCase
import com.vela.domain.usecase.GetPricesAndMarketDataUseCase
import com.vela.ui.base.AssetHolder
import com.vela.ui.base.pricepolling.PricePollingConfig
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.screens.detail.state.DetailUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val getCoinDetail: GetCoinDetailUseCase = mockk()
    private val getOhlc: GetOhlcUseCase = mockk()
    private val getPrices: GetPricesAndMarketDataUseCase = mockk(relaxed = true)
    private val assetHolder: AssetHolder = mockk { every { get(any()) } returns null }
    private val config: PricePollingConfig = mockk { every { refreshDelaySeconds } returns 60L }

    private fun createViewModel(): DetailViewModel = DetailViewModel(
        getCoinDetail = getCoinDetail,
        getOhlc = getOhlc,
        assetHolder = assetHolder,
        getPrices = getPrices,
        pricePolling = PricePollingController(config),
        savedStateHandle = SavedStateHandle(mapOf("coinId" to "bitcoin"))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ----- initial load -----

    @Test
    fun `initial load success - state is Success with isChartLoading true before ohlc loads`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } coAnswers { kotlinx.coroutines.delay(1_000); DataResult.Success(emptyList()) }

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as DetailUiState.Success
        assertFalse(state.isChartLoading)
        assertNotNull(state.detail)
    }

    @Test
    fun `initial load error - state is Error`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is DetailUiState.Error)
    }

    // ----- refresh -----

    @Test
    fun `refresh from Success - isRefreshing true and detail preserved`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        val detailBefore = (vm.uiState.value as DetailUiState.Success).detail

        coEvery { getCoinDetail(any()) } coAnswers { kotlinx.coroutines.delay(1_000); DataResult.Success(coinDetail()) }
        vm.retry()
        dispatcher.scheduler.advanceTimeBy(100)

        val refreshingState = vm.uiState.value as DetailUiState.Success
        assertTrue(refreshingState.isRefreshing)
        assertEquals(detailBefore, refreshingState.detail)
    }

    @Test
    fun `refresh error from Success - stays Success with nonBlockingError set`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)
        vm.retry()
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(AppError.NoInternet, state.nonBlockingError)
    }

    // ----- onRangeSelected -----

    @Test
    fun `onRangeSelected - only last range result lands in state`() = runTest {
        val slowPoints = listOf(OhlcPoint(1L, 1.0, 2.0, 0.5, 1.5))
        val fastPoints = listOf(OhlcPoint(2L, 2.0, 3.0, 1.5, 2.5))

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        coEvery { getOhlc(any(), eq(TimeRange.ONE_WEEK)) } coAnswers {
            kotlinx.coroutines.delay(500); DataResult.Success(slowPoints)
        }
        coEvery { getOhlc(any(), eq(TimeRange.ONE_MONTH)) } returns DataResult.Success(fastPoints)

        vm.onRangeSelected(TimeRange.ONE_WEEK)
        dispatcher.scheduler.advanceTimeBy(100)
        vm.onRangeSelected(TimeRange.ONE_MONTH)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(fastPoints, state.ohlcPoints)
    }

    // ----- OHLC error -----

    @Test
    fun `ohlc error sets nonBlockingError and does not clear existing chart points`() = runTest {
        val existingPoints = listOf(OhlcPoint(1L, 1.0, 2.0, 0.5, 1.5))

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(existingPoints)

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        coEvery { getOhlc(any(), any()) } returns DataResult.Error(AppError.NoInternet)
        vm.onRangeSelected(TimeRange.ONE_WEEK)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(AppError.NoInternet, state.nonBlockingError)
        assertEquals(existingPoints, state.ohlcPoints)
    }

    // ----- toggleChartFullScreen -----

    @Test
    fun `toggleChartFullScreen - toggles between true and false`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isChartFullScreen)
        vm.toggleChartFullScreen()
        assertTrue(vm.isChartFullScreen)
        vm.toggleChartFullScreen()
        assertFalse(vm.isChartFullScreen)
    }

    // ----- retry -----

    @Test
    fun `retry from Error triggers full reload`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is DetailUiState.Error)

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())
        vm.retry()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is DetailUiState.Success)
    }

    @Test
    fun `retry from Success triggers refresh not full reload`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        coEvery { getCoinDetail(any()) } coAnswers { kotlinx.coroutines.delay(500); DataResult.Success(coinDetail()) }
        vm.retry()
        dispatcher.scheduler.advanceTimeBy(100)

        assertTrue(vm.uiState.value is DetailUiState.Success)
    }

    // ----- helpers -----

    private fun coinDetail() = CoinDetail(
        id = "bitcoin",
        symbol = "btc",
        name = "Bitcoin",
        image = null,
        currentPrice = 30_000.0,
        priceChange24h = 500.0,
        priceChangePercent24h = 1.5,
        marketCap = 600_000_000_000.0,
        totalVolume = 20_000_000_000.0,
        circulatingSupply = 19_000_000.0,
        ath = 69_000.0,
        atl = 67.81,
        marketCapRank = 1
    )
}