package com.vela.ui.screens.home

import androidx.lifecycle.ViewModelStore
import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetTodayUseCase
import com.vela.domain.usecase.GetTopAssetsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getTopAssets: GetTopAssetsUseCase = mockk()
    private val getToday: GetTodayUseCase = mockk()

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

    @Before
    fun setup() {
        coEvery { getToday() } returns LocalDate.of(2000, 1, 1)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state emission check`() {
        coEvery { getTopAssets(any()) } returns DataResult.Success(emptyList())

        val viewModel = HomeViewModel(getTopAssets, getToday)

        // coroutine hasn't run yet — state is still Loading
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `Successful asset data mapping`() = runTest {
        val assets = listOf(anAsset())
        coEvery { getTopAssets(any()) } returns DataResult.Success(assets)

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(1, (state as HomeUiState.Success).assets.size)
        assertEquals("bitcoin", state.assets[0].id)
    }

    @Test
    fun `Date string format validation`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Success(emptyList())
        coEvery { getToday() } returns LocalDate.of(2026, 5, 26)

        val expectedDate = "Tuesday, 26 May"

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(expectedDate, state.date)
    }

    @Test
    fun `No internet connection error handling`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Error(AppError.NoInternet)

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals(AppError.NoInternet, (state as HomeUiState.Error).appError)
    }

    @Test
    fun `Server side error handling`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Error(AppError.ServerError)

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals(AppError.ServerError, (state as HomeUiState.Error).appError)
    }

    @Test
    fun `Unknown exception message propagation`() = runTest {
        val errorMessage = "Custom error message"
        coEvery { getTopAssets(any()) } returns DataResult.Error(AppError.Unknown(errorMessage))

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertTrue((state as HomeUiState.Error).appError is AppError.Unknown)
        assertEquals(errorMessage, (state.appError as AppError.Unknown).message)
    }

    @Test
    fun `Empty unknown error message handling`() = runTest {
        // AppError.Unknown.message is non-null String, so we test empty string behavior
        coEvery { getTopAssets(any()) } returns DataResult.Error(AppError.Unknown(""))

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertTrue((state as HomeUiState.Error).appError is AppError.Unknown)
        assertEquals("", (state.appError as AppError.Unknown).message)
    }

    @Test
    fun `Retry logic execution flow`() = runTest {
        coEvery { getTopAssets(any()) } returnsMany listOf(
            DataResult.Error(AppError.NoInternet),
            DataResult.Success(listOf(anAsset()))
        )

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Error)

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Success)
        coVerify(exactly = 2) { getTopAssets(any()) }
    }

    @Test
    fun `Loading state reset on retry`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Error(AppError.NoInternet)

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Error)

        coEvery { getTopAssets(any()) } returns DataResult.Success(emptyList())
        viewModel.retry()

        // before advancing — should be Loading
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `Empty list success handling`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Success(emptyList())

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertTrue((state as HomeUiState.Success).assets.isEmpty())
    }

    @Test
    fun `ViewModel initialization side effect`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Success(emptyList())

        HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { getTopAssets(any()) }
    }

    @Test
    fun `Coroutine cancellation on clear`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Success(emptyList())

        val viewModel = HomeViewModel(getTopAssets, getToday)

        // to call protected onCleared()
        val viewModelStore = ViewModelStore()
        viewModelStore.put("some_key", viewModel)

        viewModelStore.clear()

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `Date is refreshed on retry`() = runTest {
        coEvery { getTopAssets(any()) } returns DataResult.Success(emptyList())

        val viewModel = HomeViewModel(getTopAssets, getToday)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { getToday() }
    }
}