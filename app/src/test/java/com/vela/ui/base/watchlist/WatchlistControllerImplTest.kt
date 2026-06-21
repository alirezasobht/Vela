package com.vela.ui.base.watchlist

import com.vela.domain.usecase.IsWatchlistedUseCase
import com.vela.domain.usecase.ToggleWatchlistUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistControllerImplTest {
    private val isWatchlistedUseCase: IsWatchlistedUseCase = mockk()
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase = mockk()
    private val controller = WatchlistControllerImpl(isWatchlistedUseCase, toggleWatchlistUseCase)

    @Suppress("UnusedFlow")
    @Test
    fun `observeIsWatchlisted delegates to use case`() = runTest {
        every { isWatchlistedUseCase("bitcoin") } returns flowOf(true)

        val result = controller.observeIsWatchlisted("bitcoin")

        assertEquals(true, result.first())
        verify { isWatchlistedUseCase("bitcoin") }
    }

    @Test
    fun `toggleWatchlist before bind is a no-op`() = runTest {
        controller.toggleWatchlist("bitcoin")

        coVerify(exactly = 0) { toggleWatchlistUseCase(any()) }
    }

    @Test
    fun `toggleWatchlist after bind launches use case on bound scope`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(testDispatcher)
        coEvery { toggleWatchlistUseCase("bitcoin") } returns Unit

        controller.bind(scope)
        controller.toggleWatchlist("bitcoin")
        testScheduler.advanceUntilIdle()

        coVerify { toggleWatchlistUseCase("bitcoin") }
    }
}
