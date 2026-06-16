package com.vela.data.repository

import app.cash.turbine.test
import com.vela.data.source.local.dao.WatchlistDao
import com.vela.data.source.local.model.WatchlistEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WatchlistRepositoryImplTest {
    private val dao: WatchlistDao = mockk()
    private val repository = WatchlistRepositoryImpl(dao)

    @Test
    fun `toggleWatchlist inserts when not watchlisted`() =
        runTest {
            every { dao.isWatchlisted("bitcoin") } returns flowOf(false)
            coEvery { dao.insert(any()) } just runs

            repository.toggleWatchlist("bitcoin")

            coVerify { dao.insert(WatchlistEntity("bitcoin")) }
            coVerify(exactly = 0) { dao.delete(any()) }
        }

    @Test
    fun `toggleWatchlist deletes when already watchlisted`() =
        runTest {
            every { dao.isWatchlisted("bitcoin") } returns flowOf(true)
            coEvery { dao.delete(any()) } just runs

            repository.toggleWatchlist("bitcoin")

            coVerify { dao.delete("bitcoin") }
            coVerify(exactly = 0) { dao.insert(any()) }
        }

    @Test
    fun `observeWatchlist maps entities to coin ids`() =
        runTest {
            every { dao.observeAll() } returns
                flowOf(
                    listOf(WatchlistEntity("bitcoin"), WatchlistEntity("ethereum"))
                )

            repository.observeWatchlist().test {
                assertEquals(listOf("bitcoin", "ethereum"), awaitItem())
                awaitComplete()
            }
        }
}
