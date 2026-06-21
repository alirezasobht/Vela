package com.vela.data.repository

import app.cash.turbine.test
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
import org.junit.Assert.assertNull
import org.junit.Test

class AlertRepositoryImplTest {
    private val dao: AlertDao = mockk()
    private val repository = AlertRepositoryImpl(dao)

    private fun entity() = AlertEntity(
        id = 1L,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = "PRICE",
        direction = "ABOVE",
        targetValue = 70_000.0
    )

    private fun alert() = Alert(
        id = 1L,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        targetValue = 70_000.0
    )

    @Test
    fun `observeAlertsByCoinId maps entities to domain models`() = runTest {
        every { dao.observeByCoinId("bitcoin") } returns flowOf(listOf(entity()))

        repository.observeAlertsByCoinId("bitcoin").test {
            assertEquals(listOf(alert()), awaitItem())
            awaitComplete()
        }
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

    @Test
    fun `getAlertById returns null when not found`() = runTest {
        coEvery { dao.getById(99L) } returns null

        val result = repository.getAlertById(99L)

        assertNull(result)
    }
}
