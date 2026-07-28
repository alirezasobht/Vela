package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.SimplePrice
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.repository.AlertRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckAlertsAgainstPricesUseCaseTest {
    private val alertRepository: AlertRepository = mockk(relaxed = true)
    private val priceStore: SimplePriceStore = mockk()
    private val alertTriggerEvaluator: AlertTriggerEvaluator = mockk()
    private val useCase = CheckAlertsAgainstPricesUseCase(alertRepository, priceStore, alertTriggerEvaluator)

    @Test
    fun `returns triggered alerts from evaluator and marks them triggered`() = runTest {
        val alerts = listOf(alert(id = 1L), alert(id = 2L, coinId = "ethereum"))
        val prices = mapOf("bitcoin" to simplePrice(), "ethereum" to simplePrice())
        every { priceStore.getPrices() } returns prices
        coEvery { alertRepository.getActiveAlerts() } returns alerts
        every { alertTriggerEvaluator.evaluate(alerts, prices) } returns listOf(alerts[0])

        val result = useCase()

        assertEquals(listOf(alerts[0]), result)
        coVerify { alertRepository.markTriggered(1L) }
        coVerify(exactly = 0) { alertRepository.markTriggered(2L) }
    }

    @Test
    fun `returns empty and does not call evaluator when store has no prices`() = runTest {
        every { priceStore.getPrices() } returns emptyMap()

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { alertRepository.getActiveAlerts() }
        verify(exactly = 0) { alertTriggerEvaluator.evaluate(any(), any()) }
    }

    @Test
    fun `returns empty and does not call evaluator when no active alerts`() = runTest {
        every { priceStore.getPrices() } returns mapOf("bitcoin" to simplePrice())
        coEvery { alertRepository.getActiveAlerts() } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
        verify(exactly = 0) { alertTriggerEvaluator.evaluate(any(), any()) }
    }

    @Test
    fun `marks every alert returned by the evaluator as triggered`() = runTest {
        val alerts = listOf(alert(id = 1L), alert(id = 2L, coinId = "ethereum"), alert(id = 3L, coinId = "solana"))
        val prices = mapOf("bitcoin" to simplePrice(), "ethereum" to simplePrice(), "solana" to simplePrice())
        every { priceStore.getPrices() } returns prices
        coEvery { alertRepository.getActiveAlerts() } returns alerts
        every { alertTriggerEvaluator.evaluate(alerts, prices) } returns listOf(alerts[0], alerts[2])

        useCase()

        coVerify { alertRepository.markTriggered(1L) }
        coVerify { alertRepository.markTriggered(3L) }
        coVerify(exactly = 0) { alertRepository.markTriggered(2L) }
    }

    // ----- helpers -----

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
