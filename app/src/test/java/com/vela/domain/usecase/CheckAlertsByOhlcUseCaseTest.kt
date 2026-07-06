package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint
import com.vela.domain.repository.AlertRepository
import com.vela.domain.repository.DetailRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckAlertsByOhlcUseCaseTest {
    private val alertRepository: AlertRepository = mockk(relaxed = true)
    private val detailRepository: DetailRepository = mockk()
    private val useCase = CheckAlertsByOhlcUseCase(alertRepository, detailRepository)

    @Test
    fun `triggers ABOVE when candle high reaches target`() = runTest {
        val alert = alert(direction = AlertDirection.ABOVE, targetValue = 100.0)
        stubAlerts(alert)
        stubOhlc(alert.coinId, candles(high = 100.0))

        val result = useCase()

        assertEquals(listOf(alert), result)
        coVerify { alertRepository.markTriggered(alert.id) }
    }

    @Test
    fun `triggers BELOW when candle low reaches target`() = runTest {
        val alert = alert(direction = AlertDirection.BELOW, targetValue = 50.0)
        stubAlerts(alert)
        stubOhlc(alert.coinId, candles(low = 50.0))

        val result = useCase()

        assertEquals(listOf(alert), result)
        coVerify { alertRepository.markTriggered(alert.id) }
    }

    @Test
    fun `does not trigger when candle does not reach target`() = runTest {
        val alert = alert(direction = AlertDirection.ABOVE, targetValue = 100.0)
        stubAlerts(alert)
        stubOhlc(alert.coinId, candles(high = 99.9))

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { alertRepository.markTriggered(any()) }
    }

    @Test
    fun `updates lastOhlcCheckTimestamp when not triggered`() = runTest {
        val alert = alert(direction = AlertDirection.ABOVE, targetValue = 100.0)
        stubAlerts(alert)
        stubOhlc(alert.coinId, listOf(candle(timestamp = 2000L, high = 90.0)))

        useCase()

        coVerify { alertRepository.updateOhlcCheckTimestamp(alert.id, 2000L) }
    }

    @Test
    fun `does not update timestamp on trigger`() = runTest {
        val alert = alert(direction = AlertDirection.ABOVE, targetValue = 100.0)
        stubAlerts(alert)
        stubOhlc(alert.coinId, candles(high = 100.0))

        useCase()

        coVerify(exactly = 0) { alertRepository.updateOhlcCheckTimestamp(any(), any()) }
    }

    @Test
    fun `skips candles at or before lowerBound`() = runTest {
        val alert = alert(ohlcAnchorTimestamp = 1000L, lastOhlcCheckTimestamp = 1500L)
        stubAlerts(alert)
        // all candles at or before lowerBound (1500L)
        stubOhlc(
            alert.coinId,
            listOf(
                candle(timestamp = 1000L, high = 999.0),
                candle(timestamp = 1500L, high = 999.0)
            )
        )

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { alertRepository.markTriggered(any()) }
        coVerify(exactly = 0) { alertRepository.updateOhlcCheckTimestamp(any(), any()) }
    }

    @Test
    fun `uses max of ohlcAnchorTimestamp and lastOhlcCheckTimestamp as lower bound`() = runTest {
        // anchor=2000, lastCheck=500 → lowerBound=2000
        // candle at 1500 should be skipped, candle at 2500 should be checked
        val alert = alert(
            direction = AlertDirection.ABOVE,
            targetValue = 100.0,
            ohlcAnchorTimestamp = 2000L,
            lastOhlcCheckTimestamp = 500L
        )
        stubAlerts(alert)
        stubOhlc(
            alert.coinId,
            listOf(
                candle(timestamp = 1500L, high = 999.0), // before anchor → skip
                candle(timestamp = 2500L, high = 100.0) // after anchor → check
            )
        )

        val result = useCase()

        assertEquals(listOf(alert), result)
    }

    @Test
    fun `skips alert on OHLC API error`() = runTest {
        val alert = alert()
        stubAlerts(alert)
        coEvery { detailRepository.getOhlc(alert.coinId, any()) } returns DataResult.Error(AppError.ServerError)

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { alertRepository.markTriggered(any()) }
    }

    @Test
    fun `skips PERCENT type alerts`() = runTest {
        val alert = alert(type = AlertType.PERCENT)
        stubAlerts(alert)

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { detailRepository.getOhlc(any(), any()) }
    }

    @Test
    fun `returns empty list when no active alerts`() = runTest {
        coEvery { alertRepository.getActiveAlerts() } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `calls OHLC API once per coin when multiple alerts share the same coinId`() = runTest {
        stubAlerts(
            alert(id = 1L, coinId = "bitcoin", targetValue = 100.0),
            alert(id = 2L, coinId = "bitcoin", targetValue = 200.0)
        )
        stubOhlc("bitcoin", candles(high = 50.0))

        useCase()

        coVerify(exactly = 1) { detailRepository.getOhlc("bitcoin", any()) }
    }

    // ----- helpers -----

    private fun stubAlerts(vararg alerts: Alert) {
        coEvery { alertRepository.getActiveAlerts() } returns alerts.toList()
    }

    private fun stubOhlc(
        coinId: String,
        candleList: List<OhlcPoint>
    ) {
        coEvery { detailRepository.getOhlc(coinId, any()) } returns DataResult.Success(candleList)
    }

    private fun candle(
        timestamp: Long = 2000L,
        high: Double = 50.0,
        low: Double = 40.0
    ) = OhlcPoint(timestamp = timestamp, open = 45.0, high = high, low = low, close = 48.0)

    private fun candles(
        high: Double = 50.0,
        low: Double = 40.0
    ) = listOf(candle(timestamp = 2000L, high = high, low = low))

    private fun alert(
        id: Long = 1L,
        coinId: String = "bitcoin",
        type: AlertType = AlertType.PRICE,
        direction: AlertDirection = AlertDirection.ABOVE,
        targetValue: Double = 100.0,
        ohlcAnchorTimestamp: Long = 1000L,
        lastOhlcCheckTimestamp: Long = 0L
    ) = Alert(
        id = id,
        coinId = coinId,
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = type,
        direction = direction,
        targetValue = targetValue,
        ohlcAnchorTimestamp = ohlcAnchorTimestamp,
        lastOhlcCheckTimestamp = lastOhlcCheckTimestamp
    )
}
