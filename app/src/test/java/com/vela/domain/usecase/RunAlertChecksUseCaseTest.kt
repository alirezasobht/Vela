package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.notification.AlertNotifier
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RunAlertChecksUseCaseTest {
    private val checkAlertsByOhlc: CheckAlertsByOhlcUseCase = mockk()
    private val checkAlertsByMarketData: CheckAlertsByMarketDataUseCase = mockk()
    private val alertNotifier: AlertNotifier = mockk(relaxed = true)
    private val useCase = RunAlertChecksUseCase(checkAlertsByOhlc, checkAlertsByMarketData, alertNotifier)

    @Test
    fun `calls both check use cases`() = runTest {
        coEvery { checkAlertsByOhlc() } returns emptyList()
        coEvery { checkAlertsByMarketData() } returns emptyList()

        useCase()

        coVerify { checkAlertsByOhlc() }
        coVerify { checkAlertsByMarketData() }
    }

    @Test
    fun `calls market check even when ohlc returns alerts`() = runTest {
        coEvery { checkAlertsByOhlc() } returns listOf(alert(id = 1L))
        coEvery { checkAlertsByMarketData() } returns emptyList()

        useCase()

        coVerify { checkAlertsByMarketData() }
    }

    @Test
    fun `notifies for each triggered alert`() = runTest {
        val a1 = alert(id = 1L)
        val a2 = alert(id = 2L)
        coEvery { checkAlertsByOhlc() } returns listOf(a1)
        coEvery { checkAlertsByMarketData() } returns listOf(a2)

        useCase()

        verify { alertNotifier.notify(a1) }
        verify { alertNotifier.notify(a2) }
    }

    @Test
    fun `deduplicates alerts triggered by both checks`() = runTest {
        val alert = alert(id = 1L)
        coEvery { checkAlertsByOhlc() } returns listOf(alert)
        coEvery { checkAlertsByMarketData() } returns listOf(alert)

        useCase()

        verify(exactly = 1) { alertNotifier.notify(alert) }
    }

    // ----- helpers -----

    private fun alert(id: Long = 1L) = Alert(
        id = id,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        targetValue = 100.0
    )
}
