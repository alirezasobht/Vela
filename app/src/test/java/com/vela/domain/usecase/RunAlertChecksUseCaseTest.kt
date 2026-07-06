package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RunAlertChecksUseCaseTest {
    private val checkAlertsByOhlc: CheckAlertsByOhlcUseCase = mockk()
    private val checkAlertsByMarketData: CheckAlertsByMarketDataUseCase = mockk()
    private val useCase = RunAlertChecksUseCase(checkAlertsByOhlc, checkAlertsByMarketData)

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
