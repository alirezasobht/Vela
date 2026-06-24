package com.vela.ui.screens.alerts

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.Asset
import com.vela.domain.usecase.ObserveAllAlertsUseCase
import com.vela.ui.base.AssetPreviewCache
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val observeAllAlerts: ObserveAllAlertsUseCase = mockk()
    private val assetPreviewCache: AssetPreviewCache = mockk()
    private val alertLabelFormatter: AlertLabelFormatter = mockk()
    private val alertsFlow = MutableStateFlow<List<Alert>>(emptyList())

    private fun createViewModel(): AlertsViewModel = AlertsViewModel(
        observeAllAlerts = observeAllAlerts,
        assetPreviewCache = assetPreviewCache,
        alertLabelFormatter = alertLabelFormatter
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { observeAllAlerts.invoke() } returns alertsFlow
        every { alertLabelFormatter.format(any()) } returns "label"
        every { assetPreviewCache.get(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = createViewModel()

        assertEquals(AlertsUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `empty alerts emits Empty state`() = runTest {
        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AlertsUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `non-empty alerts emits Success state`() = runTest {
        val vm = createViewModel()
        alertsFlow.value = listOf(alert())
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.uiState.value is AlertsUiState.Success)
    }

    @Test
    fun `alerts are grouped by coinId`() = runTest {
        val vm = createViewModel()
        alertsFlow.value = listOf(
            alert(id = 1L, coinId = "bitcoin"),
            alert(id = 2L, coinId = "bitcoin"),
            alert(id = 3L, coinId = "ethereum")
        )
        dispatcher.scheduler.advanceUntilIdle()

        val groups = (vm.uiState.value as AlertsUiState.Success).groups
        assertEquals(2, groups.size)
        assertEquals(2, groups.first { it.coinId == "bitcoin" }.alerts.size)
        assertEquals(1, groups.first { it.coinId == "ethereum" }.alerts.size)
    }

    @Test
    fun `group uses coinName and coinSymbol from first alert in group`() = runTest {
        val vm = createViewModel()
        alertsFlow.value = listOf(alert())
        dispatcher.scheduler.advanceUntilIdle()

        val group = (vm.uiState.value as AlertsUiState.Success).groups[0]
        assertEquals("Bitcoin", group.coinName)
        assertEquals("btc", group.coinSymbol)
    }

    @Test
    fun `group coinImage is taken from AssetPreviewCache`() = runTest {
        val asset = Asset(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "https://image.url",
            currentPrice = 60_000.0,
            priceChangePercent24h = 1.0,
            marketCapRank = 1,
            sparkline = null
        )
        every { assetPreviewCache.get("bitcoin") } returns asset

        val vm = createViewModel()
        alertsFlow.value = listOf(alert())
        dispatcher.scheduler.advanceUntilIdle()

        val group = (vm.uiState.value as AlertsUiState.Success).groups[0]
        assertEquals("https://image.url", group.coinImage)
    }

    @Test
    fun `group coinImage is null when asset not in cache`() = runTest {
        val vm = createViewModel()
        alertsFlow.value = listOf(alert())
        dispatcher.scheduler.advanceUntilIdle()

        val group = (vm.uiState.value as AlertsUiState.Success).groups[0]
        assertEquals(null, group.coinImage)
    }

    @Test
    fun `alert rows use label from alertLabelFormatter`() = runTest {
        every { alertLabelFormatter.format(any()) } returns "Price above \$80,000"

        val vm = createViewModel()
        alertsFlow.value = listOf(alert())
        dispatcher.scheduler.advanceUntilIdle()

        val row = (vm.uiState.value as AlertsUiState.Success).groups[0].alerts[0]
        assertEquals("Price above \$80,000", row.label)
    }

    @Test
    fun `state updates when alerts flow emits new list`() = runTest {
        val vm = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(AlertsUiState.Empty, vm.uiState.value)

        alertsFlow.value = listOf(alert())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, vm.uiState.value is AlertsUiState.Success)

        alertsFlow.value = emptyList()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(AlertsUiState.Empty, vm.uiState.value)
    }

    // ----- helpers -----

    private fun alert(
        id: Long = 1L,
        coinId: String = "bitcoin"
    ) = Alert(
        id = id,
        coinId = coinId,
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        targetValue = 80_000.0
    )
}
