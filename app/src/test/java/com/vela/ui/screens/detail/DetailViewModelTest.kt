package com.vela.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.CoinDetail
import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint
import com.vela.domain.model.TimeRange
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.usecase.GetCoinDetailUseCase
import com.vela.domain.usecase.GetOhlcUseCase
import com.vela.domain.usecase.IsWatchlistedUseCase
import com.vela.domain.usecase.ObserveAlertsByCoinIdUseCase
import com.vela.domain.usecase.ToggleWatchlistUseCase
import com.vela.ui.base.AssetPreviewCache
import com.vela.ui.navigation.Screen
import com.vela.ui.screens.alerts.AlertLabelFormatter
import com.vela.ui.screens.alerts.AlertRowUiModel
import com.vela.ui.screens.detail.state.AlertFormState
import com.vela.ui.screens.detail.state.DetailAction
import com.vela.ui.screens.detail.state.DetailContentState
import com.vela.ui.screens.detail.state.DetailTab
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
    private val assetPreviewCache: AssetPreviewCache = mockk()
    private val priceStore: SimplePriceStore = mockk()
    private val pricePolling: PricePolling = mockk(relaxed = true)
    private val errorFlow = MutableSharedFlow<AppError?>(replay = 1)
    private val isWatchlistedUseCase: IsWatchlistedUseCase = mockk()
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase = mockk()
    private val observeAlertsByCoinId: ObserveAlertsByCoinIdUseCase = mockk()
    private val alertLabelFormatter: AlertLabelFormatter = mockk()
    private fun createViewModel(initialTab: DetailTab = DetailTab.STATS): DetailViewModel {
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        val handle = mockk<SavedStateHandle>()
        every { handle.toRoute<Screen.CoinDetail>() } returns Screen.CoinDetail(
            coinId = "bitcoin",
            initialTab = initialTab,
            initialAlertId = null
        )
        return DetailViewModel(
            getCoinDetail = getCoinDetail,
            getOhlc = getOhlc,
            assetPreviewCache = assetPreviewCache,
            priceStore = priceStore,
            pricePolling = pricePolling,
            isWatchlistedUseCase = isWatchlistedUseCase,
            toggleWatchlistUseCase = toggleWatchlistUseCase,
            observeAlertsByCoinId = observeAlertsByCoinId,
            alertLabelFormatter = alertLabelFormatter,
            savedStateHandle = handle
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { assetPreviewCache.get(any()) } returns null
        every { isWatchlistedUseCase.invoke(any()) } returns flowOf(false)
        every { observeAlertsByCoinId.invoke(any()) } returns flowOf(emptyList())
        every { priceStore.observePrice(any()) } returns flowOf(null)
        every { pricePolling.observeError() } returns errorFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
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

        val content = vm.state.value.content as DetailContentState.Success
        assertEquals(false, content.isChartLoading)
        assertNotNull(content.detail)
    }

    @Test
    fun `initial load error - state is Error`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.content is DetailContentState.Error)
    }

    // ----- refresh -----

    @Test
    fun `refresh from Success - isRefreshing true and detail preserved`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        val detailBefore = (vm.state.value.content as DetailContentState.Success).detail

        coEvery { getCoinDetail(any()) } coAnswers {
            kotlinx.coroutines.delay(1_000.milliseconds)
            DataResult.Success(coinDetail())
        }
        vm.onAction(DetailAction.Retry)
        dispatcher.scheduler.advanceTimeBy(100)

        val content = vm.state.value.content as DetailContentState.Success
        assertEquals(true, content.isRefreshing)
        assertEquals(detailBefore, content.detail)
    }

    @Test
    fun `refresh error from Success - stays Success with nonBlockingError set`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)
        vm.onAction(DetailAction.Retry)
        dispatcher.scheduler.advanceUntilIdle()

        val content = vm.state.value.content as DetailContentState.Success
        assertEquals(false, content.isRefreshing)
        assertEquals(AppError.NoInternet, content.nonBlockingError)
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

        vm.onAction(DetailAction.RangeSelected(TimeRange.SEVEN_DAYS))
        dispatcher.scheduler.advanceTimeBy(100)
        vm.onAction(DetailAction.RangeSelected(TimeRange.ONE_MONTH))
        dispatcher.scheduler.advanceUntilIdle()

        val content = vm.state.value.content as DetailContentState.Success
        assertEquals(fastPoints, content.ohlcPoints)
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
        vm.onAction(DetailAction.RangeSelected(TimeRange.SEVEN_DAYS))
        dispatcher.scheduler.advanceUntilIdle()

        val content = vm.state.value.content as DetailContentState.Success
        assertEquals(AppError.NoInternet, content.nonBlockingError)
        assertEquals(existingPoints, content.ohlcPoints)
    }

    // ----- toggleChartFullScreen -----

    @Test
    fun `toggleChartFullScreen - toggles between true and false`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, vm.state.value.isChartFullScreen)
        vm.onAction(DetailAction.ToggleFullScreen)
        assertEquals(true, vm.state.value.isChartFullScreen)
        vm.onAction(DetailAction.ToggleFullScreen)
        assertEquals(false, vm.state.value.isChartFullScreen)
    }

    // ----- retry -----

    @Test
    fun `retry from Error triggers full reload`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.content is DetailContentState.Error)

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())
        vm.onAction(DetailAction.Retry)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.content is DetailContentState.Success)
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
        vm.onAction(DetailAction.Retry)
        dispatcher.scheduler.advanceTimeBy(100)

        assertTrue(vm.state.value.content is DetailContentState.Success)
    }

    // ----- watchlist -----

    @Test
    fun `isWatchlisted initial value is false`() = runTest {
        every { isWatchlistedUseCase.invoke(any()) } returns flowOf(true)
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()

        assertEquals(false, vm.state.value.isWatchlisted)
    }

    @Test
    fun `isWatchlisted reflects IsWatchlistedUseCase flow value`() = runTest {
        every { isWatchlistedUseCase.invoke(any()) } returns flowOf(true)
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.state.value.isWatchlisted)
    }

    @Test
    fun `toggleWatchlist calls ToggleWatchlistUseCase with correct coinId`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())
        coEvery { toggleWatchlistUseCase.invoke("bitcoin") } returns Unit

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onAction(DetailAction.ToggleWatchlist)
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
    fun `register is called with the coin id`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { pricePolling.register(setOf("bitcoin")) }
    }

    @Test
    fun `price polling error updates nonBlockingError in Success state`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        errorFlow.tryEmit(AppError.NoInternet)
        dispatcher.scheduler.advanceUntilIdle()

        val content = vm.state.value.content as DetailContentState.Success
        assertEquals(AppError.NoInternet, content.nonBlockingError)
    }

    @Test
    fun `onRangeSelected updates selectedRange state`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        vm.onAction(DetailAction.RangeSelected(TimeRange.ONE_YEAR))

        assertEquals(TimeRange.ONE_YEAR, vm.state.value.selectedRange)
    }

    // ----- alerts -----

    @Test
    fun `openAlertForm increments formSessionId on subsequent calls`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onAction(DetailAction.EditAlert(null))
        val first = (vm.state.value.alertFormState as AlertFormState.Visible).formSessionId

        vm.onAction(DetailAction.EditAlert(null))
        val second = (vm.state.value.alertFormState as AlertFormState.Visible).formSessionId

        assertEquals(first + 1, second)
    }

    @Test
    fun `openAlertForm sets alertFormState to Visible`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onAction(DetailAction.EditAlert(null))

        assertTrue(vm.state.value.alertFormState is AlertFormState.Visible)
    }

    @Test
    fun `openAlertForm sets correct alertRowUiModel for existing alert`() = runTest {
        val alert = Alert(
            id = 42L,
            coinId = "bitcoin",
            coinName = "Bitcoin",
            coinSymbol = "btc",
            type = AlertType.PRICE,
            direction = AlertDirection.ABOVE,
            targetValue = 80_000.0
        )
        every { observeAlertsByCoinId.invoke(any()) } returns flowOf(listOf(alert))
        every { alertLabelFormatter.format(any()) } returns "BTC > $80,000"

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onAction(DetailAction.EditAlert(42L))

        val formState = vm.state.value.alertFormState as AlertFormState.Visible
        assertEquals(42L, formState.alertRowUiModel?.id)
        assertEquals("BTC > $80,000", formState.alertRowUiModel?.label)
    }

    @Test
    fun `openAlertForm with unknown id sets null alertRowUiModel`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onAction(DetailAction.EditAlert(null))

        val formState = vm.state.value.alertFormState as AlertFormState.Visible
        assertEquals(null, formState.alertRowUiModel)
    }

    @Test
    fun `dismissAlertForm sets alertFormState to Invisible`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onAction(DetailAction.EditAlert(null))
        assertTrue(vm.state.value.alertFormState is AlertFormState.Visible)

        vm.onAction(DetailAction.DismissAlertForm)
        assertTrue(vm.state.value.alertFormState is AlertFormState.Invisible)
    }

    @Test
    fun `observeAlerts updates Success state alerts on change`() = runTest {
        val alertsFlow = MutableStateFlow<List<Alert>>(emptyList())
        every { observeAlertsByCoinId.invoke(any()) } returns alertsFlow

        coEvery { getCoinDetail(any()) } returns DataResult.Success(coinDetail())
        coEvery { getOhlc(any(), any()) } returns DataResult.Success(emptyList())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<AlertRowUiModel>(), (vm.state.value.content as DetailContentState.Success).alerts)

        val alert = Alert(
            id = 1L,
            coinId = "bitcoin",
            coinName = "Bitcoin",
            coinSymbol = "btc",
            type = AlertType.PRICE,
            direction = AlertDirection.ABOVE,
            targetValue = 80_000.0
        )
        every { alertLabelFormatter.format(any()) } returns "BTC > $80,000"
        alertsFlow.value = listOf(alert)
        dispatcher.scheduler.advanceUntilIdle()

        val alerts = (vm.state.value.content as DetailContentState.Success).alerts
        assertEquals(1, alerts.size)
        assertEquals(1L, alerts[0].id)
        assertEquals("BTC > $80,000", alerts[0].label)
    }

    // ----- nav graph alert transition -----

    @Test
    fun `useNavGraphAlertTransition is false when initialTab is STATS`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel(DetailTab.STATS)

        assertFalse(vm.useNavGraphAlertTransition.value)
    }

    @Test
    fun `useNavGraphAlertTransition is true when initialTab is ALERTS`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel(DetailTab.ALERTS)

        assertTrue(vm.useNavGraphAlertTransition.value)
    }

    @Test
    fun `onNavGraphTransitionFinished resets useNavGraphAlertTransition to false`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel(DetailTab.ALERTS)
        assertEquals(true, vm.useNavGraphAlertTransition.value)

        vm.onNavGraphTransitionFinished()

        assertEquals(false, vm.useNavGraphAlertTransition.value)
    }

    @Test
    fun `onNavGraphTransitionFinished does not reset transition when back navigation is pending`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel(DetailTab.ALERTS)
        vm.onAction(DetailAction.Back)
        vm.onNavGraphTransitionFinished()

        assertTrue(vm.useNavGraphAlertTransition.value)
    }

    @Test
    fun `Back action sends navigateBack event`() = runTest {
        coEvery { getCoinDetail(any()) } returns DataResult.Error(AppError.NoInternet)

        val vm = createViewModel()
        var navigatedBack = false
        val job = launch { vm.navigateBack.collect { navigatedBack = true } }

        vm.onAction(DetailAction.Back)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(navigatedBack)
        job.cancel()
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
