package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.repository.AlertRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EditAlertUseCaseTest {
    private val repository: AlertRepository = mockk(relaxed = true)
    private val useCase = EditAlertUseCase(repository)

    @Test
    fun `id is 0 calls createAlert`() = runTest {
        useCase(alert(id = 0L))
        coVerify { repository.createAlert(any()) }
        coVerify(exactly = 0) { repository.updateAlert(any()) }
    }

    @Test
    fun `id is non-zero calls updateAlert`() = runTest {
        useCase(alert(id = 1L))
        coVerify { repository.updateAlert(any()) }
        coVerify(exactly = 0) { repository.createAlert(any()) }
    }

    @Test
    fun `create passes alert unchanged to repository`() = runTest {
        val alert = alert(id = 0L)
        useCase(alert)
        coVerify { repository.createAlert(alert) }
    }

    @Test
    fun `update passes alert unchanged to repository`() = runTest {
        val alert = alert(id = 42L)
        useCase(alert)
        coVerify { repository.updateAlert(alert) }
    }

    // ----- helpers -----

    private fun alert(id: Long = 0L) = Alert(
        id = id,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PERCENT,
        direction = AlertDirection.ABOVE,
        targetValue = 5.0
    )
}
