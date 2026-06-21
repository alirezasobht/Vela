package com.vela.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.CoinDetail
import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint
import com.vela.domain.model.TimeRange
import com.vela.domain.usecase.GetCoinDetailUseCase
import com.vela.domain.usecase.GetOhlcUseCase
import com.vela.domain.usecase.GetPricesAndMarketDataUseCase
import com.vela.domain.usecase.IsWatchlistedUseCase
import com.vela.domain.usecase.ObserveAlertsByCoinIdUseCase
import com.vela.domain.usecase.ToggleWatchlistUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.base.pricepolling.PricePollingConfig
import com.vela.ui.base.pricepolling.PricePollingController
import com.vela.ui.screens.detail.state.DetailUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val getCoinDetail: GetCoinDetailUseCase = mockk()
    private val getOhlc: GetOhlcUseCase = mockk()
    private val getPrices: GetPricesAndMarketDataUseCase = mockk(relaxed = true)
    private val assetPreviewCache: AssetPreviewCache = mockk()
    private val config: PricePollingConfig = mockk { every { refreshDelaySeconds } returns 60L }
    private val isWatchlistedUseCase: IsWatchlistedUseCase = mockk()
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase = mockk()
    private val observeAlertsByCoinId: ObserveAlertsByCoinIdUseCase = mockk()

    private fun createViewModel(): DetailViewModel {
        val handle = SavedStateHandle(mapOf("coinId" to "bitcoin"))
        return DetailViewModel(
            getCoinDetail = getCoinDetail,
            getOhlc = getOhlc,
            assetPreviewCache = assetPreviewCache,
            getPrices = getPrices,
            pricePolling = PricePollingController(config),
            isWatchlistedUseCase = isWatchlistedUseCase,
            toggleWatchlistUseCase = toggleWatchlistUseCase,
            observeAlertsByCoinId = observeAlertsByCoinId,
            savedStateHandle = handle
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { assetPreviewCache.get(any()) } returns null
        every { isWatchlistedUseCase.invoke(any()) } returns flowOf(false)
        every { observeAlertsByCoinId.invoke(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ----- initial load -----

    @Test
    fun `initial load success - state is Success with isChartLoading false after ohlc loads`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1_000.milliseconds)
            DataResult.Success(emptyList())
        }

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(false, state.isChartLoading)
        assertNotNull(state.detail)
    }

    @Test
    fun `initial load error - state is Error`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.uiState.value is DetailUiState.Error)
    }

    // ----- refresh -----

    @Test
    fun `refresh from Success - isRefreshing true and detail preserved`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        val detailBefore = (vm.uiState.value as DetailUiState.Success).detail

        coEvery { getCoinDetail(any()) } coAnswers {
            kotlinx.coroutines.delay(1_000.milliseconds)
            DataResult.Success(coinDetail())
        }
        vm.retry()
        dispatcher.scheduler.advanceTimeBy(100)

        val refreshingState = vm.uiState.value as DetailUiState.Success
        assertEquals(true, refreshingState.isRefreshing)
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
        assertEquals(false, state.isRefreshing)
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

        coEvery { getOhlc(any(), eq(TimeRange.SEVEN_DAYS)) } coAnswers {
            kotlinx.coroutines.delay(500.milliseconds)
            DataResult.Success(slowPoints)
        }
        coEvery { getOhlc(any(), eq(TimeRange.ONE_MONTH)) } returns DataResult.Success(fastPoints)

        vm.onRangeSelected(TimeRange.SEVEN_DAYS)
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
        vm.onRangeSelected(TimeRange.SEVEN_DAYS)
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

        assertEquals(false, vm.isChartFullScreen)
        vm.toggleChartFullScreen()
        assertEquals(true, vm.isChartFullScreen)
        vm.toggleChartFullScreen()
        assertEquals(false, vm.isChartFullScreen)
    }

    // ----- retry -----

    @Test
    fun `retry from Error triggers full reload`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, vm.uiState.value is DetailUiState.Error)

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())
        vm.retry()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.uiState.value is DetailUiState.Success)
    }

    @Test
    fun `retry from Success triggers refresh not full reload`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        coEvery { getCoinDetail(any()) } coAnswers {
            kotlinx.coroutines.delay(500.milliseconds)
            DataResult.Success(coinDetail())
        }
        vm.retry()
        dispatcher.scheduler.advanceTimeBy(100)

        assertEquals(true, vm.uiState.value is DetailUiState.Success)
    }

    // ----- watchlist -----

    @Test
    fun `isWatchlisted initial value is false`() = runTest {
        every { isWatchlistedUseCase.invoke(any()) } returns flowOf(true)
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()

        assertEquals(false, vm.isWatchlisted.value)
    }

    @Test
    fun `isWatchlisted reflects IsWatchlistedUseCase flow value`() = runTest {
        every { isWatchlistedUseCase.invoke(any()) } returns flowOf(true)
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        val job = launch { vm.isWatchlisted.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.isWatchlisted.value)
        job.cancel()
    }

    @Test
    fun `toggleWatchlist calls ToggleWatchlistUseCase with correct coinId`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toggleWatchlistUseCase.invoke("bitcoin") } returns Unit

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.toggleWatchlist()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { toggleWatchlistUseCase.invoke("bitcoin") }
    }

    // ----- cache and polling -----

    @Test
    fun `initialHeader is populated from cache if available`() = runTest {
        val cachedAsset = Asset(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "img_url",
            currentPrice = 60000.0,
            priceChangePercent24h = 2.0,
            marketCapRank = 1,
            sparkline = null
        )
        every { assetPreviewCache.get("bitcoin") } returns cachedAsset
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()

        assertNotNull(vm.initialHeader)
        assertEquals("Bitcoin", vm.initialHeader?.name)
        assertEquals("img_url", vm.initialHeader?.image)
    }

    @Test
    fun `onPriceError updates nonBlockingError in Success state`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onPriceError(AppError.NoInternet)

        val state = vm.uiState.value as DetailUiState.Success
        assertEquals(AppError.NoInternet, state.nonBlockingError)
    }

    @Test
    fun `onRangeSelected updates selectedRange state`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        vm.onRangeSelected(TimeRange.ONE_YEAR)

        assertEquals(TimeRange.ONE_YEAR, vm.selectedRange)
    }

    // ----- alerts -----

    @Test
    fun `openAlertForm increments formSessionId`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        val before = vm.formSessionId

        vm.openAlertForm()

        assertEquals(before + 1, vm.formSessionId)
    }

    @Test
    fun `openAlertForm sets isAlertFormVisible to true`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.openAlertForm()

        assertEquals(true, vm.isAlertFormVisible)
    }

    @Test
    fun `openAlertForm emits alertFormEvent with correct id`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        var emittedId: Long? = -99L
        val job = launch { vm.alertFormEvent.collect { emittedId = it } }
        dispatcher.scheduler.advanceUntilIdle()

        vm.openAlertForm(id = 42L)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(42L, emittedId)
        job.cancel()
    }

    @Test
    fun `openAlertForm is no-op when isChartFullScreen is true`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.toggleChartFullScreen()
        val sessionBefore = vm.formSessionId
        vm.openAlertForm()

        assertEquals(false, vm.isAlertFormVisible)
        assertEquals(sessionBefore, vm.formSessionId)
    }

    @Test
    fun `dismissAlertForm sets isAlertFormVisible to false`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.openAlertForm()
        assertEquals(true, vm.isAlertFormVisible)

        vm.dismissAlertForm()
        assertEquals(false, vm.isAlertFormVisible)
    }

    @Test
    fun `observeAlerts updates Success state alerts on change`() = runTest {
        val alertsFlow = MutableStateFlow<List<Alert>>(emptyList())
        every { observeAlertsByCoinId.invoke(any()) } returns alertsFlow

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<Alert>(), (vm.uiState.value as DetailUiState.Success).alerts)

        val alert = Alert(
            id = 1L,
            coinId = "bitcoin",
            coinName = "Bitcoin",
            coinSymbol = "btc",
            type = AlertType.PRICE,
            direction = AlertDirection.ABOVE,
            targetValue = 80_000.0
        )
        alertsFlow.value = listOf(alert)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(alert), (vm.uiState.value as DetailUiState.Success).alerts)
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
