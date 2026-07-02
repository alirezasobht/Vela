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
import org.junit.Test

class EditAlertUseCaseTest {
    private val repository: AlertRepository = mockk(relaxed = true)
    private val detailRepository: DetailRepository = mockk()
    private val useCase = EditAlertUseCase(repository, detailRepository)

    private fun stubOhlc(timestamp: Long = 1000L) {
        coEvery { detailRepository.getOhlc(any(), any()) } returns
            DataResult.Success(listOf(OhlcPoint(timestamp, 0.0, 0.0, 0.0, 0.0)))
    }

    @Test
    fun `id is 0 calls createAlert`() = runTest {
        stubOhlc()
        useCase(alert(id = 0L))
        coVerify { repository.createAlert(any()) }
        coVerify(exactly = 0) { repository.updateAlert(any()) }
    }

    @Test
    fun `id is non-zero calls updateAlert`() = runTest {
        stubOhlc()
        useCase(alert(id = 1L))
        coVerify { repository.updateAlert(any()) }
        coVerify(exactly = 0) { repository.createAlert(any()) }
    }

    @Test
    fun `create sets ohlcAnchorTimestamp from last candle`() = runTest {
        stubOhlc(timestamp = 1000L)
        val alert = alert(id = 0L)
        useCase(alert)
        coVerify { repository.createAlert(alert.copy(ohlcAnchorTimestamp = 1000L)) }
    }

    @Test
    fun `update resets isTriggered and sets ohlcAnchorTimestamp from last candle`() = runTest {
        stubOhlc(timestamp = 1000L)
        val alert = alert(id = 42L, isTriggered = true)
        useCase(alert)
        coVerify { repository.updateAlert(alert.copy(isTriggered = false, ohlcAnchorTimestamp = 1000L)) }
    }

    @Test
    fun `create uses 0L as ohlcAnchorTimestamp on OHLC error`() = runTest {
        coEvery { detailRepository.getOhlc(any(), any()) } returns DataResult.Error(AppError.ServerError)
        val alert = alert(id = 0L)
        useCase(alert)
        coVerify { repository.createAlert(alert.copy(ohlcAnchorTimestamp = 0L)) }
    }

    @Test
    fun `create uses 0L as ohlcAnchorTimestamp on empty candle list`() = runTest {
        coEvery { detailRepository.getOhlc(any(), any()) } returns DataResult.Success(emptyList())
        val alert = alert(id = 0L)
        useCase(alert)
        coVerify { repository.createAlert(alert.copy(ohlcAnchorTimestamp = 0L)) }
    }

    // ----- helpers -----

    private fun alert(
        id: Long = 0L,
        isTriggered: Boolean = false
    ) = Alert(
        id = id,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PERCENT,
        direction = AlertDirection.ABOVE,
        targetValue = 5.0,
        isTriggered = isTriggered
    )
}
