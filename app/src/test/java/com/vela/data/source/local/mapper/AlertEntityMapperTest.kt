package com.vela.data.source.local.mapper

import com.vela.data.source.local.model.AlertEntity
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertEntityMapperTest {
    private fun entity(
        id: Long = 1L,
        type: String = "PRICE",
        direction: String = "ABOVE",
        targetValue: Double = 70_000.0,
        isTriggered: Boolean = false
    ) = AlertEntity(
        id = id,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = type,
        direction = direction,
        targetValue = targetValue,
        isTriggered = isTriggered
    )

    private fun alert(
        id: Long = 1L,
        type: AlertType = AlertType.PRICE,
        direction: AlertDirection = AlertDirection.ABOVE,
        targetValue: Double = 70_000.0,
        isTriggered: Boolean = false
    ) = Alert(
        id = id,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = type,
        direction = direction,
        targetValue = targetValue,
        isTriggered = isTriggered
    )

    @Test
    fun `toDomain maps all fields correctly`() {
        val result = entity().toDomain()
        assertEquals(alert(), result)
    }

    @Test
    fun `toEntity stores enum as name string`() {
        val result = alert().toEntity()
        assertEquals("PRICE", result.type)
        assertEquals("ABOVE", result.direction)
    }

    @Test
    fun `toDomain toEntity roundtrip preserves all values`() {
        val original = entity(id = 5L, type = "PERCENT", direction = "BELOW", targetValue = 3.5, isTriggered = true)
        val roundtrip = original.toDomain().toEntity()
        assertEquals(original, roundtrip)
    }
}
