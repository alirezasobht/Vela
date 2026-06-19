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

    private fun alert(id: Long) =
        Alert(
            id = id,
            coinId = "bitcoin",
            coinName = "Bitcoin",
            coinSymbol = "btc",
            type = AlertType.PRICE,
            direction = AlertDirection.ABOVE,
            targetValue = 70_000.0
        )

    @Test
    fun `id 0L calls createAlert`() =
        runTest {
            useCase(alert(id = 0L))
            coVerify { repository.createAlert(any()) }
            coVerify(exactly = 0) { repository.updateAlert(any()) }
        }

    @Test
    fun `non-zero id calls updateAlert`() =
        runTest {
            useCase(alert(id = 42L))
            coVerify { repository.updateAlert(any()) }
            coVerify(exactly = 0) { repository.createAlert(any()) }
        }
}
