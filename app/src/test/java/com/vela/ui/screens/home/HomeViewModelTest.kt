package com.vela.ui.screens.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.domain.usecase.GetTodayUseCase
import com.vela.domain.usecase.GetTopAssetsUseCase
import com.vela.domain.usecase.RefreshAssetsUseCase
import com.vela.ui.base.AssetHolder
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getTopAssets: GetTopAssetsUseCase = mockk()
    private val refreshAssets: RefreshAssetsUseCase = mockk()
    private val getToday: GetTodayUseCase = mockk()
    private val getPrices: GetPricesOnlyUseCase = mockk()
    private val assetHolder = AssetHolder()

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

    private fun createViewModel() = HomeViewModel(
        getTopAssets = getTopAssets,
        refreshAssets = refreshAssets,
        getToday = getToday,
        assetHolder = assetHolder,
        getPrices = getPrices,
        savedStateHandle = SavedStateHandle(mapOf("delay" to Long.MAX_VALUE))
    )

    @Before
    fun setup() {
        every { getToday() } returns LocalDate.of(2000, 1, 1)
        every { getTopAssets(any()) } returns flowOf(DataResult.Success(emptyList()))
        coEvery { refreshAssets(any()) } returns DataResult.Success(Unit)
        coEvery { getPrices(any()) } returns DataResult.Success(emptyMap())
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state emission check`() {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `Successful asset data mapping`() = runTest {
        val assets = listOf(anAsset())
        every { getTopAssets(any()) } returns flowOf(DataResult.Success(assets))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(1, (state as HomeUiState.Success).assets.size)
        assertEquals("bitcoin", state.assets[0].id)
    }

    @Test
    fun `formattedDate string format validation`() = runTest {
        every { getToday() } returns LocalDate.of(2026, 5, 26)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals("Tuesday, 26 May", state.formattedDate)
    }

    @Test
    fun `No internet connection error handling`() = runTest {
        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.NoInternet)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals(AppError.NoInternet, (state as HomeUiState.Error).appError)
    }

    @Test
    fun `Server side error handling`() = runTest {
        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.ServerError)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals(AppError.ServerError, (state as HomeUiState.Error).appError)
    }

    @Test
    fun `Unknown exception message propagation`() = runTest {
        val errorMessage = "Custom error message"
        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.Unknown(errorMessage))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertTrue((state as HomeUiState.Error).appError is AppError.Unknown)
        assertEquals(errorMessage, (state.appError as AppError.Unknown).message)
    }

    @Test
    fun `Empty unknown error message handling`() = runTest {
        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.Unknown(""))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("", ((state as HomeUiState.Error).appError as AppError.Unknown).message)
    }

    @Test
    fun `Retry logic execution flow`() = runTest {
        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.NoInternet)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.Error)

        coEvery { refreshAssets(any()) } returns DataResult.Success(Unit)
        every { getTopAssets(any()) } returns flowOf(DataResult.Success(listOf(anAsset())))

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }

    @Test
    fun `Loading state reset on retry`() = runTest {
        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.NoInternet)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.Error)

        coEvery { refreshAssets(any()) } coAnswers {
            kotlinx.coroutines.delay(1000) // Simulate work
            DataResult.Success(Unit)
        }

        viewModel.retry()

        testDispatcher.scheduler.runCurrent()

        // before advancing — should be Loading
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `Empty list success handling`() = runTest {
        every { getTopAssets(any()) } returns flowOf(DataResult.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertTrue((state as HomeUiState.Success).assets.isEmpty())
    }

    @Test
    fun `ViewModel initialization side effect`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { refreshAssets(any()) }
    }

    @Test
    fun `Coroutine cancellation on clear`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBeforeClear = viewModel.uiState.value

        val viewModelStore = ViewModelStore()
        viewModelStore.put("key", viewModel)
        viewModelStore.clear()
        testDispatcher.scheduler.advanceUntilIdle()

        // state doesn't reset on clear — stays as it was
        assertEquals(stateBeforeClear, viewModel.uiState.value)
        // no more refreshes after clear
        coVerify(exactly = 1) { refreshAssets(any()) }
    }

    @Test
    fun `formattedDate is refreshed on retry`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(atLeast = 2) { getToday() }
    }

    @Test
    fun `onLimitChanged resets state to Loading`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.Success)

        viewModel.onLimitChanged(25)

        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `onLimitChanged updates selectedLimit`() = runTest {
        val viewModel = createViewModel()

        viewModel.onLimitChanged(25)

        assertEquals(25, viewModel.selectedLimit)
    }

    @Test
    fun `onLimitChanged triggers fresh refresh with new limit`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onLimitChanged(25)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { refreshAssets(25) }
    }

    @Test
    fun `pullToRefresh sets pullRefreshing to true`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.pullToRefresh()

        assertTrue(viewModel.pullRefreshing.value)
    }

    @Test
    fun `pullToRefresh resets pullRefreshing after completion`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.pullToRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.pullRefreshing.value)
    }

    @Test
    fun `nonBlockingError set when refresh fails after success`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.Success)

        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.NoInternet)
        viewModel.pullToRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(AppError.NoInternet, (state as HomeUiState.Success).nonBlockingError)
    }

    @Test
    fun `nonBlockingError cleared on successful refresh`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.NoInternet)
        viewModel.pullToRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { refreshAssets(any()) } returns DataResult.Success(Unit)
        viewModel.pullToRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(null, state.nonBlockingError)
    }

    @Test
    fun `Mutex concurrency exclusion`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { refreshAssets(any()) } coAnswers {
            kotlinx.coroutines.delay(500)
            DataResult.Success(Unit)
        }

        vm.pullToRefresh()
        testDispatcher.scheduler.runCurrent() // starts first refresh, suspends at delay

        vm.pullToRefresh() // mutex locked — skipped
        testDispatcher.scheduler.runCurrent()

        testDispatcher.scheduler.advanceUntilIdle()

        // 1 initial + 1 pull (second pull skipped by mutex)
        coVerify(exactly = 2) { refreshAssets(any()) }
    }

    @Test
    fun `observeAssets reactive update with preserved error`() = runTest {
        val assetsFlow = kotlinx.coroutines.flow.MutableSharedFlow<DataResult<List<Asset>>>(replay = 1)
        every { getTopAssets(any()) } returns assetsFlow
        assetsFlow.emit(DataResult.Success(listOf(anAsset())))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is HomeUiState.Success)

        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.NoInternet)
        vm.pullToRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateWithError = vm.uiState.value as HomeUiState.Success
        assertEquals(AppError.NoInternet, stateWithError.nonBlockingError)

        // Room emits new data — nonBlockingError should be preserved
        assetsFlow.emit(DataResult.Success(listOf(anAsset("ethereum"))))
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = vm.uiState.value as HomeUiState.Success
        assertEquals("ethereum", updatedState.assets[0].id)
        assertEquals(AppError.NoInternet, updatedState.nonBlockingError)
    }

    @Test
    fun `observeAssets error mapping during initial loading`() = runTest {
        every { getTopAssets(any()) } returns flowOf(DataResult.Error(AppError.ServerError))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals(AppError.ServerError, (state as HomeUiState.Error).appError)
    }

    @Test
    fun `observeAssets error suppression in Success state`() = runTest {
        val assetsFlow = kotlinx.coroutines.flow.MutableSharedFlow<DataResult<List<Asset>>>(replay = 1)
        every { getTopAssets(any()) } returns assetsFlow
        assetsFlow.emit(DataResult.Success(listOf(anAsset())))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is HomeUiState.Success)

        assetsFlow.emit(DataResult.Error(AppError.ServerError))
        testDispatcher.scheduler.advanceUntilIdle()

        // State stays Success, error suppressed
        assertTrue(vm.uiState.value is HomeUiState.Success)
    }

    @Test
    fun `doRefresh skip loading state if Success exists`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is HomeUiState.Success)

        coEvery { refreshAssets(any()) } coAnswers {
            kotlinx.coroutines.delay(500)
            DataResult.Success(Unit)
        }

        vm.pullToRefresh()
        testDispatcher.scheduler.runCurrent() // start refresh, suspend at delay

        // Still Success — no Loading flicker
        assertTrue(vm.uiState.value is HomeUiState.Success)

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `pullToRefresh spinner reset on unexpected exception`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { refreshAssets(any()) } throws RuntimeException("Unexpected error")

        vm.pullToRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // _pullRefreshing must be reset even on exception
        assertFalse(vm.pullRefreshing.value)
    }

    @Test
    fun `retry execution with modified selectedLimit`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        clearMocks(refreshAssets, answers = false)

        vm.onLimitChanged(100)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { refreshAssets(100) }
        coVerify(exactly = 0) { refreshAssets(50) }
    }

    @Test
    fun `Rapid onLimitChanged calls concurrency handling`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onLimitChanged(25)
        vm.onLimitChanged(50)
        vm.onLimitChanged(100)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(100, vm.selectedLimit)
        coVerify { refreshAssets(100) }
    }

    @Test
    fun `Auto refresh loop termination on scope destruction`() = runTest {
        val delay = 1000L
        val vm = HomeViewModel(
            getTopAssets = getTopAssets,
            refreshAssets = refreshAssets,
            getToday = getToday,
            assetHolder = assetHolder,
            getPrices = getPrices,
            savedStateHandle = SavedStateHandle(mapOf("delay" to delay))
        )
        testDispatcher.scheduler.runCurrent()
        coVerify(exactly = 1) { refreshAssets(any()) }

        val viewModelStore = ViewModelStore()
        viewModelStore.put("key", vm)
        viewModelStore.clear()

        testDispatcher.scheduler.advanceTimeBy(delay * 3)
        testDispatcher.scheduler.runCurrent()

        // Only initial refresh — loop terminated
        coVerify(exactly = 1) { refreshAssets(any()) }
    }

    @Test
    fun `Success state preservation after refresh failure`() = runTest {
        val assets = listOf(anAsset())
        every { getTopAssets(any()) } returns flowOf(DataResult.Success(assets))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { refreshAssets(any()) } returns DataResult.Error(AppError.NoInternet)
        vm.pullToRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(1, state.assets.size)
        assertEquals(AppError.NoInternet, state.nonBlockingError)
    }

    @Test
    fun `observeAssets mapping with correct today date`() = runTest {
        every { getToday() } returns LocalDate.of(2026, 5, 26)
        every { getTopAssets(any()) } returns flowOf(DataResult.Success(listOf(anAsset())))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals("Tuesday, 26 May", state.formattedDate)
        verify(atLeast = 1) { getToday() }
    }
}