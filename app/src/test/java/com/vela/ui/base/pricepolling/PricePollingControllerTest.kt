package com.vela.ui.base.pricepolling

import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.usecase.GetPricesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class PricePollingControllerTest {

    private val getPrices: GetPricesUseCase = mockk()
    private val delegate: PricePollingDelegate = mockk(relaxed = true)
    private val config: PricePollingConfig = mockk {
        every { refreshDelaySeconds } returns 5L
    }

    private fun TestScope.createController(): PricePollingController {
        val controller = PricePollingController(config)
        controller.bind(
            scope = this,
            getPrices = getPrices,
            delegate = delegate
        )
        return controller
    }

    @Test
    fun `polling fires after delay`() = runTest {
        coEvery { delegate.getIds() } returns listOf("bitcoin")
        coEvery { getPrices(any()) } returns DataResult.Success(emptyMap())

        val controller = createController()
        controller.onScreenVisible(true)

        advanceTimeBy(5_001.milliseconds)
        runCurrent()

        coVerify(atLeast = 1) { getPrices(listOf("bitcoin")) }
        controller.cancel()
    }

    @Test
    fun `polling does not fire before delay`() = runTest {
        coEvery { delegate.getIds() } returns listOf("bitcoin")
        coEvery { getPrices(any()) } returns DataResult.Success(emptyMap())

        val controller = createController()
        controller.onScreenVisible(true)

        advanceTimeBy(4_999.milliseconds)

        coVerify(exactly = 0) { getPrices(any()) }
        controller.cancel()
    }

    @Test
    fun `polling pauses when screen not visible`() = runTest {
        coEvery { delegate.getIds() } returns listOf("bitcoin")
        coEvery { getPrices(any()) } returns DataResult.Success(emptyMap())

        val controller = createController()
        controller.onScreenVisible(false)

        advanceTimeBy(20_000.milliseconds)

        coVerify(exactly = 0) { getPrices(any()) }
        controller.cancel()
    }

    @Test
    fun `polling resumes when screen becomes visible`() = runTest {
        coEvery { delegate.getIds() } returns listOf("bitcoin")
        coEvery { getPrices(any()) } returns DataResult.Success(emptyMap())

        val controller = createController()
        controller.onScreenVisible(false)
        advanceTimeBy(10_000.milliseconds)
        coVerify(exactly = 0) { getPrices(any()) }

        controller.onScreenVisible(true)
        advanceTimeBy(1.milliseconds)

        coVerify(atLeast = 1) { getPrices(any()) }
        controller.cancel()
    }

    @Test
    fun `on success delegate onPriceError called with null`() = runTest {
        coEvery { delegate.getIds() } returns listOf("bitcoin")
        coEvery { getPrices(any()) } returns DataResult.Success(
            mapOf("bitcoin" to SimplePrice(price = 60000.0, priceChange = 1.0, marketCap = null, totalVolume = null))
        )

        val controller = createController()
        controller.onScreenVisible(true)
        advanceTimeBy(5_001.milliseconds)
        runCurrent()

        coVerify { delegate.onPriceError(null) }
        controller.cancel()
    }

    @Test
    fun `on error delegate onPriceError called with error`() = runTest {
        coEvery { delegate.getIds() } returns listOf("bitcoin")
        coEvery { getPrices(any()) } returns DataResult.Error(AppError.NoInternet)

        val controller = createController()
        controller.onScreenVisible(true)
        advanceTimeBy(5_001.milliseconds)
        runCurrent()

        coVerify { delegate.onPriceError(AppError.NoInternet) }
        controller.cancel()
    }

    @Test
    fun `polling stops after cancel`() = runTest {
        coEvery { delegate.getIds() } returns listOf("bitcoin")
        coEvery { getPrices(any()) } returns DataResult.Success(emptyMap())

        val controller = createController()
        controller.onScreenVisible(true)
        advanceTimeBy(5_001.milliseconds)
        runCurrent()
        coVerify(exactly = 1) { getPrices(any()) }

        controller.cancel()
        advanceTimeBy(15_000.milliseconds)
        runCurrent()

        coVerify(exactly = 1) { getPrices(any()) }
    }

    @Test
    fun `observePrice emits null for unknown id`() = runTest {
        val controller = PricePollingController(config)
        val result = mutableListOf<Any?>()
        val job = backgroundScope.launch { controller.observePrice("unknown").collect { result.add(it) } }
        runCurrent()
        Assert.assertNull(result.firstOrNull())
        job.cancel()
    }

    @Test
    fun `default refresh delay is five seconds`() {
        Assert.assertEquals(5L, PricePollingConfig().refreshDelaySeconds)
    }
}