package com.vela.data.worker

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.usecase.ObserveAllAlertsUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertCoinRegistrarTest {
    private val observeAllAlerts: ObserveAllAlertsUseCase = mockk()
    private val pricePolling: PricePolling = mockk(relaxed = true)
    private val alertsFlow = MutableStateFlow<List<Alert>>(emptyList())

    private fun TestScope.createRegistrar(): AlertCoinRegistrar {
        every { observeAllAlerts() } returns alertsFlow
        return AlertCoinRegistrar(
            observeAllAlerts = observeAllAlerts,
            pricePolling = pricePolling,
            scope = backgroundScope
        )
    }

    private fun alert(coinId: String) = Alert(
        id = 1L,
        coinId = coinId,
        coinName = coinId,
        coinSymbol = coinId,
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        targetValue = 1.0
    )

    @Test
    fun `registers coin ids from current alerts on start`() = runTest {
        alertsFlow.value = listOf(alert("bitcoin"), alert("ethereum"))
        val registrar = createRegistrar()

        registrar.start()
        runCurrent()

        coVerify { pricePolling.register(setOf("bitcoin", "ethereum")) }
    }

    @Test
    fun `registers newly created alert coin id`() = runTest {
        val registrar = createRegistrar()
        registrar.start()
        runCurrent()

        alertsFlow.value = listOf(alert("solana"))
        runCurrent()

        coVerify { pricePolling.register(setOf("solana")) }
    }

    @Test
    fun `does not register when there are no alerts`() = runTest {
        val registrar = createRegistrar()

        registrar.start()
        runCurrent()

        coVerify(exactly = 0) { pricePolling.register(any()) }
    }

    @Test
    fun `calling start twice does not start a second collector`() = runTest {
        alertsFlow.value = listOf(alert("bitcoin"))
        val registrar = createRegistrar()

        registrar.start()
        registrar.start()
        runCurrent()

        coVerify(exactly = 1) { pricePolling.register(setOf("bitcoin")) }
    }
}
