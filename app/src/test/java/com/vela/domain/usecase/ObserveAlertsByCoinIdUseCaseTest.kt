package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveAlertsByCoinIdUseCaseTest {
    private val repository: AlertRepository = mockk()
    private val useCase = ObserveAlertsByCoinIdUseCase(repository)

    @Test
    fun `delegates to repository observeAlertsByCoinId`() {
        val flow = flowOf(emptyList<Alert>())
        every { repository.observeAlertsByCoinId("bitcoin") } returns flow

        val result = useCase("bitcoin")

        assertEquals(flow, result)
        verify { repository.observeAlertsByCoinId("bitcoin") }
    }
}
