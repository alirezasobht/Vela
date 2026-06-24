package com.vela.data.repository

import app.cash.turbine.test
import com.vela.data.cache.AlertsCache
import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.model.AlertEntity
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
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

class AlertRepositoryImplTest {
    private val dao: AlertDao = mockk()
    private val cache: AlertsCache = mockk()
    private val repository = AlertRepositoryImpl(dao, cache)

    private fun entity() = AlertEntity(
        id = 1L,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = "PRICE",
        direction = "ABOVE",
        targetValue = 70_000.0
    )

    private fun alert(coinId: String = "bitcoin") = Alert(
        id = 1L,
        coinId = coinId,
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        targetValue = 70_000.0
    )

    // ----- observe -----

    @Test
    fun `observeAlertsByCoinId filters alerts by coinId`() = runTest {
        every { cache.getAlerts() } returns flowOf(
            listOf(alert("bitcoin"), alert("ethereum").copy(id = 2L))
        )

        repository.observeAlertsByCoinId("bitcoin").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("bitcoin", result[0].coinId)
            awaitComplete()
        }
    }

    @Test
    fun `observeAlertsByCoinId emits empty list when no alerts match coinId`() = runTest {
        every { cache.getAlerts() } returns flowOf(listOf(alert("ethereum")))

        repository.observeAlertsByCoinId("bitcoin").test {
            assertEquals(emptyList<Alert>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeAllAlerts returns all alerts from cache`() = runTest {
        every { cache.getAlerts() } returns flowOf(listOf(alert("bitcoin"), alert("ethereum").copy(id = 2L)))

        repository.observeAllAlerts().test {
            assertEquals(2, awaitItem().size)
            awaitComplete()
        }
    }

    // ----- snapshot -----

    @Test
    fun `getAlertsSnapshot returns filtered alerts for coinId`() {
        every { cache.getSnapshot() } returns listOf(alert("bitcoin"), alert("ethereum").copy(id = 2L))

        val result = repository.getAlertsSnapshot("bitcoin")

        assertEquals(1, result.size)
        assertEquals("bitcoin", result[0].coinId)
    }

    @Test
    fun `getAlertsSnapshot returns empty list when no alerts match coinId`() {
        every { cache.getSnapshot() } returns listOf(alert("ethereum"))

        val result = repository.getAlertsSnapshot("bitcoin")

        assertEquals(emptyList<Alert>(), result)
    }

    // ----- write ops -----

    @Test
    fun `getAlertById returns null when not found`() = runTest {
        coEvery { dao.getById(99L) } returns null

        assertEquals(null, repository.getAlertById(99L))
    }

    @Test
    fun `getAlertById returns mapped alert when found`() = runTest {
        coEvery { dao.getById(1L) } returns entity()

        assertEquals(alert(), repository.getAlertById(1L))
    }

    @Test
    fun `createAlert calls dao insert with correct entity`() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        repository.createAlert(alert())

        coVerify { dao.insert(entity()) }
    }

    @Test
    fun `updateAlert calls dao update with correct entity`() = runTest {
        coEvery { dao.update(any()) } just runs

        repository.updateAlert(alert())

        coVerify { dao.update(entity()) }
    }

    @Test
    fun `deleteAlert calls dao delete with correct id`() = runTest {
        coEvery { dao.delete(1L) } just runs

        repository.deleteAlert(1L)

        coVerify { dao.delete(1L) }
    }
}
