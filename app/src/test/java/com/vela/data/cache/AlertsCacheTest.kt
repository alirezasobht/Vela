package com.vela.data.cache

import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.model.AlertEntity
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertsCacheTest {
    private val dao: AlertDao = mockk()

    @Test
    fun `getAlerts loads database snapshot when cache has not emitted yet`() = runTest {
        every { dao.observeAll() } returns MutableSharedFlow()
        coEvery { dao.getAll() } returns listOf(alertEntity(100L))

        val cache = AlertsCache(dao)

        val result = cache.getAlerts().first()

        assertEquals(1, result.size)
        assertEquals(100L, result[0].id)
    }

    @Test
    fun `getAlerts emits cached value when cache has emitted`() = runTest {
        every { dao.observeAll() } returns MutableStateFlow(
            listOf(alertEntity(100L), alertEntity(200L))
        )
        coEvery { dao.getAll() } returns listOf(alertEntity(300L))

        val cache = AlertsCache(dao)

        val result = cache.getAlerts()

        assertEquals(2, result.first().size)
        assertEquals(100L, result.first()[0].id)
        assertEquals(200L, result.first()[1].id)
    }

    private fun alertEntity(id: Long) = AlertEntity(
        id = id,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PRICE.name,
        direction = AlertDirection.ABOVE.name,
        targetValue = 80_000.0
    )
}
