package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.repository.AlertRepository
import com.vela.domain.repository.PriceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckAlertsByMarketDataUseCaseTest {
    private val alertRepository: AlertRepository = mockk(relaxed = true)
    private val priceRepository: PriceRepository = mockk()
    private val useCase = CheckAlertsByMarketDataUseCase(alertRepository, priceRepository)

    @Test
    fun `triggers PRICE ABOVE when price reaches target`() = runTest {
        stubAlerts(alert(type = AlertType.PRICE, direction = AlertDirection.ABOVE, targetValue = 100.0))
        stubPrices("bitcoin" to simplePrice(price = 100.0))

        val result = useCase()

        assertEquals(1, result.size)
        coVerify { alertRepository.markTriggered(1L) }
    }

    @Test
    fun `triggers PRICE BELOW when price reaches target`() = runTest {
        stubAlerts(alert(type = AlertType.PRICE, direction = AlertDirection.BELOW, targetValue = 50.0))
        stubPrices("bitcoin" to simplePrice(price = 50.0))

        val result = useCase()

        assertEquals(1, result.size)
    }

    @Test
    fun `does not trigger PRICE when price has not reached target`() = runTest {
        stubAlerts(alert(type = AlertType.PRICE, direction = AlertDirection.ABOVE, targetValue = 100.0))
        stubPrices("bitcoin" to simplePrice(price = 99.9))

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { alertRepository.markTriggered(any()) }
    }

    @Test
    fun `triggers PERCENT ABOVE when 24h change reaches target`() = runTest {
        stubAlerts(alert(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, targetValue = 5.0))
        stubPrices("bitcoin" to simplePrice(priceChange = 5.0))

        val result = useCase()

        assertEquals(1, result.size)
        coVerify { alertRepository.markTriggered(1L) }
    }

    @Test
    fun `triggers PERCENT BELOW when 24h change reaches target`() = runTest {
        stubAlerts(alert(type = AlertType.PERCENT, direction = AlertDirection.BELOW, targetValue = -5.0))
        stubPrices("bitcoin" to simplePrice(priceChange = -5.0))

        val result = useCase()

        assertEquals(1, result.size)
    }

    @Test
    fun `does not trigger PERCENT when change has not reached target`() = runTest {
        stubAlerts(alert(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, targetValue = 5.0))
        stubPrices("bitcoin" to simplePrice(priceChange = 4.9))

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skips alert when coin not in price response`() = runTest {
        stubAlerts(alert(coinId = "bitcoin"))
        stubPrices("ethereum" to simplePrice())

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { alertRepository.markTriggered(any()) }
    }

    @Test
    fun `skips PRICE alert when price is null`() = runTest {
        stubAlerts(alert(type = AlertType.PRICE, targetValue = 1.0))
        stubPrices("bitcoin" to simplePrice(price = null))

        assertTrue(useCase().isEmpty())
    }

    @Test
    fun `skips PERCENT alert when priceChange is null`() = runTest {
        stubAlerts(alert(type = AlertType.PERCENT, targetValue = 1.0))
        stubPrices("bitcoin" to simplePrice(priceChange = null))

        assertTrue(useCase().isEmpty())
    }

    @Test
    fun `returns empty on API error`() = runTest {
        stubAlerts(alert())
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Error(AppError.ServerError)

        assertTrue(useCase().isEmpty())
    }

    @Test
    fun `returns empty when no active alerts`() = runTest {
        coEvery { alertRepository.getActiveAlertsSnapshot() } returns emptyList()

        assertTrue(useCase().isEmpty())
        coVerify(exactly = 0) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `fetches prices for distinct coin IDs only`() = runTest {
        stubAlerts(
            alert(id = 1L, coinId = "bitcoin"),
            alert(id = 2L, coinId = "bitcoin"),
            alert(id = 3L, coinId = "ethereum")
        )
        stubPrices("bitcoin" to simplePrice(), "ethereum" to simplePrice())

        useCase()

        coVerify(exactly = 1) { priceRepository.getPrices(match { it.size == 2 && it.containsAll(listOf("bitcoin", "ethereum")) }, any()) }
    }

    // ----- helpers -----

    private fun stubAlerts(vararg alerts: Alert) {
        coEvery { alertRepository.getActiveAlertsSnapshot() } returns alerts.toList()
    }

    private fun stubPrices(vararg pairs: Pair<String, SimplePrice>) {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(pairs.toMap())
    }

    private fun simplePrice(
        price: Double? = 80_000.0,
        priceChange: Double? = 2.0
    ) = SimplePrice(price = price, priceChange = priceChange, marketCap = null, totalVolume = null)

    private fun alert(
        id: Long = 1L,
        coinId: String = "bitcoin",
        type: AlertType = AlertType.PRICE,
        direction: AlertDirection = AlertDirection.ABOVE,
        targetValue: Double = 100.0
    ) = Alert(
        id = id,
        coinId = coinId,
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = type,
        direction = direction,
        targetValue = targetValue
    )
}
