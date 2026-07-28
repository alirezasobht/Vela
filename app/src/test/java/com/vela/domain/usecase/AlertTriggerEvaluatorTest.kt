package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.model.SimplePrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertTriggerEvaluatorTest {
    private val evaluator = AlertTriggerEvaluator()

    @Test
    fun `triggers PRICE ABOVE when price reaches target`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PRICE, direction = AlertDirection.ABOVE, targetValue = 100.0)),
            mapOf("bitcoin" to simplePrice(price = 100.0))
        )

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun `triggers PRICE BELOW when price reaches target`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PRICE, direction = AlertDirection.BELOW, targetValue = 50.0)),
            mapOf("bitcoin" to simplePrice(price = 50.0))
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `does not trigger PRICE when price has not reached target`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PRICE, direction = AlertDirection.ABOVE, targetValue = 100.0)),
            mapOf("bitcoin" to simplePrice(price = 99.9))
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `triggers PERCENT ABOVE when 24h change reaches target`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, targetValue = 5.0)),
            mapOf("bitcoin" to simplePrice(priceChange = 5.0))
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `triggers PERCENT BELOW when 24h change reaches target`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PERCENT, direction = AlertDirection.BELOW, targetValue = -5.0)),
            mapOf("bitcoin" to simplePrice(priceChange = -5.0))
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `does not trigger PERCENT when change has not reached target`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, targetValue = 5.0)),
            mapOf("bitcoin" to simplePrice(priceChange = 4.9))
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skips alert when coin not in prices map`() {
        val result = evaluator.evaluate(
            listOf(alert(coinId = "bitcoin")),
            mapOf("ethereum" to simplePrice())
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skips PRICE alert when price is null`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PRICE, targetValue = 1.0)),
            mapOf("bitcoin" to simplePrice(price = null))
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skips PERCENT alert when priceChange is null`() {
        val result = evaluator.evaluate(
            listOf(alert(type = AlertType.PERCENT, targetValue = 1.0)),
            mapOf("bitcoin" to simplePrice(priceChange = null))
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty when alerts list is empty`() {
        val result = evaluator.evaluate(emptyList(), mapOf("bitcoin" to simplePrice()))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty when prices map is empty`() {
        val result = evaluator.evaluate(listOf(alert()), emptyMap())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `evaluates multiple alerts for distinct coins independently`() {
        val result = evaluator.evaluate(
            listOf(
                alert(id = 1L, coinId = "bitcoin", targetValue = 100.0),
                alert(id = 2L, coinId = "ethereum", targetValue = 200.0)
            ),
            mapOf(
                "bitcoin" to simplePrice(price = 100.0),
                "ethereum" to simplePrice(price = 150.0)
            )
        )

        assertEquals(listOf(1L), result.map { it.id })
    }

    // ----- helpers -----

    private fun simplePrice(
        price: Double? = 80_000.0,
        priceChange: Double? = 2.0
    ) = SimplePrice(price = price, priceChange = priceChange, marketCap = null, totalVolume = null)

    private fun alert(
        id: Long = 1L,
        coinId: String = "bitcoin",
        type: AlertType = AlertType.PRICE,
        direction: AlertDirection = AlertDirection.ABOVE,
        targetValue: Double = 100.0
    ) = Alert(
        id = id,
        coinId = coinId,
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = type,
        direction = direction,
        targetValue = targetValue
    )
}
