package com.vela.ui.screens.alerts

import com.vela.R
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.usecase.EditAlertUseCase
import com.vela.domain.usecase.GetAlertByIdUseCase
import com.vela.domain.usecase.GetPricesOnlyUseCase
import com.vela.domain.usecase.ValidateAlertUseCase
import com.vela.ui.base.AssetPreviewCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertFormViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val assetPreviewCache: AssetPreviewCache = mockk()
    private val getPricesOnly: GetPricesOnlyUseCase = mockk()
    private val editAlert: EditAlertUseCase = mockk(relaxed = true)
    private val getAlertById: GetAlertByIdUseCase = mockk()
    private val validateAlert: ValidateAlertUseCase = ValidateAlertUseCase()

    private val asset = Asset(
        id = "bitcoin",
        symbol = "btc",
        name = "Bitcoin",
        image = null,
        currentPrice = 60_000.0,
        priceChangePercent24h = 1.0,
        marketCapRank = 1,
        sparkline = null
    )

    private fun createViewModel(alertId: Long? = null): AlertFormViewModel = AlertFormViewModel(
        assetPreviewCache = assetPreviewCache,
        getPricesOnly = getPricesOnly,
        editAlert = editAlert,
        getAlertById = getAlertById,
        validateAlert = validateAlert,
        coinId = "bitcoin",
        alertId = alertId
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { assetPreviewCache.get("bitcoin") } returns asset
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ----- asset null -----

    @Test
    fun `asset null emits dismiss immediately`() = runTest {
        every { assetPreviewCache.get("bitcoin") } returns null

        val vm = createViewModel()
        var dismissed = false
        val job = launch { vm.dismissEvent.collect { dismissed = true } }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, dismissed)
        job.cancel()
    }

    // ----- create mode -----

    @Test
    fun `create mode fetchInitialPrice pre-populates value and enables input`() = runTest {
        coEvery { getPricesOnly(any()) } returns
            DataResult.Success(mapOf("bitcoin" to SimplePrice(price = 67_420.0, priceChange = 1.0, marketCap = 100_000_000.0, totalVolume = 100_000.0, priceChange24hAbsolute = 1.0)))

        val vm = createViewModel(alertId = null)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("67420", state.value)
        assertEquals(true, state.isValueInputEnabled)
    }

    @Test
    fun `create mode null price leaves value empty and input still enabled`() = runTest {
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel(alertId = null)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("", state.value)
        assertEquals(true, state.isValueInputEnabled)
    }

    @Test
    fun `create mode editBtnResId is action_create_alert`() = runTest {
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel(alertId = null)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(R.string.action_create_alert, vm.uiState.value.editBtnResId)
    }

    // ----- edit mode -----

    @Test
    fun `edit mode loadAlert pre-populates type direction and value`() = runTest {
        val alert = Alert(
            id = 1L,
            coinId = "bitcoin",
            coinName = "Bitcoin",
            coinSymbol = "btc",
            type = AlertType.PERCENT,
            direction = AlertDirection.ABOVE,
            targetValue = 5.0
        )
        coEvery { getAlertById(1L) } returns alert
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel(alertId = 1L)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(AlertType.PERCENT, state.type)
        assertEquals(AlertDirection.ABOVE, state.direction)
        assertEquals("5", state.value)
        assertEquals(true, state.isValueInputEnabled)
    }

    @Test
    fun `edit mode loadAlert returns null emits dismiss`() = runTest {
        coEvery { getAlertById(99L) } returns null
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel(alertId = 99L)
        var dismissed = false
        val job = launch { vm.dismissEvent.collect { dismissed = true } }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, dismissed)
        job.cancel()
    }

    @Test
    fun `edit mode editBtnResId is action_edit_alert`() = runTest {
        coEvery { getAlertById(1L) } returns
            Alert(
                id = 1L,
                coinId = "bitcoin",
                coinName = "Bitcoin",
                coinSymbol = "btc",
                type = AlertType.PRICE,
                direction = AlertDirection.ABOVE,
                targetValue = 70_000.0
            )
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel(alertId = 1L)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(R.string.action_edit_alert, vm.uiState.value.editBtnResId)
    }

    // ----- field changes -----

    @Test
    fun `onTypeSelected updates type and revalidates`() = runTest {
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onTypeSelected(AlertType.PERCENT)

        assertEquals(AlertType.PERCENT, vm.uiState.value.type)
    }

    @Test
    fun `onDirectionSelected updates direction and revalidates`() = runTest {
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onDirectionSelected(AlertDirection.BELOW)

        assertEquals(AlertDirection.BELOW, vm.uiState.value.direction)
    }

    @Test
    fun `onValueChanged updates value and revalidates`() = runTest {
        coEvery { getPricesOnly(any()) } returns DataResult.Success(emptyMap())

        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onValueChanged("5")

        assertEquals("5", vm.uiState.value.value)
    }

    // ----- onConfirm -----

    @Test
    fun `onConfirm in create mode calls EditAlertUseCase with id 0`() = runTest {
        coEvery { getPricesOnly(any()) } returns
            DataResult.Success(mapOf("bitcoin" to SimplePrice(price = 60_000.0, priceChange = 1.0, marketCap = 100_000_000.0, totalVolume = 100_000.0, priceChange24hAbsolute = 1.0)))

        val vm = createViewModel(alertId = null)
        dispatcher.scheduler.advanceUntilIdle()

        vm.onTypeSelected(AlertType.PERCENT)
        vm.onDirectionSelected(AlertDirection.ABOVE)
        vm.onValueChanged("5")
        vm.onConfirm()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { editAlert(match { it.id == 0L }) }
    }

    @Test
    fun `onConfirm in edit mode calls EditAlertUseCase with actual alertId`() = runTest {
        coEvery { getAlertById(1L) } returns
            Alert(
                id = 1L,
                coinId = "bitcoin",
                coinName = "Bitcoin",
                coinSymbol = "btc",
                type = AlertType.PRICE,
                direction = AlertDirection.ABOVE,
                targetValue = 70_000.0
            )
        coEvery { getPricesOnly(any()) } returns
            DataResult.Success(mapOf("bitcoin" to SimplePrice(price = 60_000.0, priceChange = 1.0, marketCap = 100_000_000.0, totalVolume = 100_000.0, priceChange24hAbsolute = 1.0)))

        val vm = createViewModel(alertId = 1L)
        dispatcher.scheduler.advanceUntilIdle()

        vm.onValueChanged("80000")
        vm.onConfirm()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { editAlert(match { it.id == 1L }) }
    }

    @Test
    fun `onConfirm sends dismiss event after success`() = runTest {
        coEvery { getPricesOnly(any()) } returns
            DataResult.Success(mapOf("bitcoin" to SimplePrice(price = 60_000.0, priceChange = 1.0, marketCap = 100_000_000.0, totalVolume = 100_000.0, priceChange24hAbsolute = 1.0)))

        val vm = createViewModel(alertId = null)
        dispatcher.scheduler.advanceUntilIdle()

        vm.onTypeSelected(AlertType.PERCENT)
        vm.onDirectionSelected(AlertDirection.ABOVE)
        vm.onValueChanged("5")

        var dismissed = false
        val job = launch { vm.dismissEvent.collect { dismissed = true } }

        vm.onConfirm()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, dismissed)
        job.cancel()
    }
}
