package com.vela.data.pricepolling

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.notification.AlertNotifier
import com.vela.domain.pricepolling.PricePollingConfig
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.repository.PriceRepository
import com.vela.domain.usecase.CheckAlertsAgainstPricesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PricePollingImplTest {
    private val priceRepository: PriceRepository = mockk()
    private val priceStore: SimplePriceStore = mockk(relaxed = true)
    private val config: PricePollingConfig = mockk {
        every { refreshDelaySeconds } returns 5L
    }
    private val checkAlertsAgainstPrices: CheckAlertsAgainstPricesUseCase = mockk(relaxed = true)
    private val alertNotifier: AlertNotifier = mockk(relaxed = true)

    private fun TestScope.createPolling(): PricePollingImpl = PricePollingImpl(
        priceRepository = priceRepository,
        priceStore = priceStore,
        config = config,
        checkAlertsAgainstPrices = checkAlertsAgainstPrices,
        alertNotifier = alertNotifier,
        scope = backgroundScope
    )

    @Test
    fun `register triggers an immediate fetch`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()

        coVerify(exactly = 1) { priceRepository.getPrices(listOf("bitcoin"), includeMarketData = true) }
    }

    @Test
    fun `register with empty ids does nothing`() = runTest {
        val polling = createPolling()
        polling.register(emptySet())
        runCurrent()

        coVerify(exactly = 0) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `no registration means no fetch ever`() = runTest {
        val polling = createPolling()
        polling.onScreenVisible(true)

        advanceTimeBy(20_000.milliseconds)
        runCurrent()

        coVerify(exactly = 0) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `next scheduled poll fires after delay`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.onScreenVisible(true)
        polling.register(setOf("bitcoin"))
        runCurrent()
        coVerify(exactly = 1) { priceRepository.getPrices(any(), any()) }

        advanceTimeBy(5_001.milliseconds)
        runCurrent()

        coVerify(exactly = 2) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `scheduled poll does not fire before delay elapses`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.onScreenVisible(true)
        polling.register(setOf("bitcoin"))
        runCurrent()

        advanceTimeBy(4_999.milliseconds)
        runCurrent()

        coVerify(exactly = 1) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `scheduled poll pauses when screen not visible`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.onScreenVisible(false)
        polling.register(setOf("bitcoin"))
        runCurrent()
        coVerify(exactly = 1) { priceRepository.getPrices(any(), any()) }

        advanceTimeBy(20_000.milliseconds)
        runCurrent()

        coVerify(exactly = 1) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `scheduled poll resumes when screen becomes visible`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.onScreenVisible(false)
        polling.register(setOf("bitcoin"))
        runCurrent()
        advanceTimeBy(10_000.milliseconds)
        runCurrent()
        coVerify(exactly = 1) { priceRepository.getPrices(any(), any()) }

        polling.onScreenVisible(true)
        advanceTimeBy(1.milliseconds)
        runCurrent()

        coVerify(exactly = 2) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `re-registering resets the schedule timer`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.onScreenVisible(true)

        polling.register(setOf("bitcoin"))
        runCurrent()
        coVerify(exactly = 1) { priceRepository.getPrices(any(), any()) }

        advanceTimeBy(3_000.milliseconds)

        polling.register(setOf("ethereum"))
        runCurrent()
        coVerify(exactly = 2) { priceRepository.getPrices(any(), any()) }

        advanceTimeBy(4_000.milliseconds)
        runCurrent()
        coVerify(exactly = 2) { priceRepository.getPrices(any(), any()) }

        advanceTimeBy(1_001.milliseconds)
        runCurrent()
        coVerify(exactly = 3) { priceRepository.getPrices(any(), any()) }
    }

    @Test
    fun `register accumulates ids across calls`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()
        polling.register(setOf("ethereum"))
        runCurrent()

        coVerify { priceRepository.getPrices(match { it.toSet() == setOf("bitcoin", "ethereum") }, includeMarketData = true) }
    }

    @Test
    fun `on success upserts into store`() = runTest {
        val price = SimplePrice(price = 60000.0, priceChange = 1.0, marketCap = null, totalVolume = null)
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(mapOf("bitcoin" to price))

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()

        coVerify { priceStore.upsert(mapOf("bitcoin" to price)) }
    }

    @Test
    fun `on error does not upsert`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Error(AppError.NoInternet)

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()

        coVerify(exactly = 0) { priceStore.upsert(any()) }
    }

    @Test
    fun `on error emits error from observeError`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Error(AppError.NoInternet)

        val polling = createPolling()
        val errors = mutableListOf<AppError?>()
        val job = backgroundScope.launch { polling.observeError().collect { errors.add(it) } }

        polling.register(setOf("bitcoin"))
        runCurrent()

        assertEquals(AppError.NoInternet, errors.last())
        job.cancel()
    }

    @Test
    fun `error clears after a successful fetch`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Error(AppError.NoInternet)

        val polling = createPolling()
        polling.onScreenVisible(true)
        val errors = mutableListOf<AppError?>()
        val job = backgroundScope.launch { polling.observeError().collect { errors.add(it) } }

        polling.register(setOf("bitcoin"))
        runCurrent()
        assertEquals(AppError.NoInternet, errors.last())

        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())
        advanceTimeBy(5_001.milliseconds)
        runCurrent()

        assertEquals(null, errors.last())
        job.cancel()
    }

    @Test
    fun `repeated identical outcomes each emit separately`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())

        val polling = createPolling()
        polling.onScreenVisible(true)
        val errors = mutableListOf<AppError?>()
        val job = backgroundScope.launch { polling.observeError().collect { errors.add(it) } }

        polling.register(setOf("bitcoin"))
        runCurrent()
        advanceTimeBy(5_001.milliseconds)
        runCurrent()

        assertEquals(listOf(null, null), errors)
        job.cancel()
    }

    @Test
    fun `on success runs alert check against fetched prices`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())
        coEvery { checkAlertsAgainstPrices() } returns emptyList()

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()

        coVerify { checkAlertsAgainstPrices() }
    }

    @Test
    fun `on success notifies each triggered alert`() = runTest {
        val triggeredAlert = Alert(
            id = 1L,
            coinId = "bitcoin",
            coinName = "Bitcoin",
            coinSymbol = "btc",
            type = AlertType.PRICE,
            direction = AlertDirection.ABOVE,
            targetValue = 90_000.0
        )
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())
        coEvery { checkAlertsAgainstPrices() } returns listOf(triggeredAlert)

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()

        verify { alertNotifier.notify(triggeredAlert) }
    }

    @Test
    fun `on success with no triggered alerts does not notify`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Success(emptyMap())
        coEvery { checkAlertsAgainstPrices() } returns emptyList()

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()

        verify(exactly = 0) { alertNotifier.notify(any()) }
    }

    @Test
    fun `on error does not run alert check`() = runTest {
        coEvery { priceRepository.getPrices(any(), any()) } returns DataResult.Error(AppError.NoInternet)

        val polling = createPolling()
        polling.register(setOf("bitcoin"))
        runCurrent()

        coVerify(exactly = 0) { checkAlertsAgainstPrices() }
        verify(exactly = 0) { alertNotifier.notify(any()) }
    }
}
