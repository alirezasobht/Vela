package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateAlertUseCaseTest {
    private val useCase = ValidateAlertUseCase()

    // ----- basic validation -----

    @Test
    fun `returns false when type is null`() {
        assertEquals(false, useCase(type = null, direction = AlertDirection.ABOVE, value = "5"))
    }

    @Test
    fun `returns false when direction is null`() {
        assertEquals(false, useCase(type = AlertType.PERCENT, direction = null, value = "5"))
    }

    @Test
    fun `returns false when value is not a number`() {
        assertEquals(false, useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "abc"))
    }

    @Test
    fun `returns false when value is empty`() {
        assertEquals(false, useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = ""))
    }

    // ----- percent type -----

    @Test
    fun `percent above valid value returns true`() {
        assertEquals(true, useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "5"))
    }

    @Test
    fun `percent below valid value returns true`() {
        assertEquals(true, useCase(type = AlertType.PERCENT, direction = AlertDirection.BELOW, value = "10"))
    }

    @Test
    fun `percent with decimal value returns true`() {
        assertEquals(true, useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "2.5"))
    }

    // ----- price type -----

    @Test
    fun `price above current price returns true`() {
        assertEquals(
            true,
            useCase(
                type = AlertType.PRICE,
                direction = AlertDirection.ABOVE,
                value = "80000",
                currentPrice = 70_000.0
            )
        )
    }

    @Test
    fun `price below current price returns true`() {
        assertEquals(
            true,
            useCase(
                type = AlertType.PRICE,
                direction = AlertDirection.BELOW,
                value = "60000",
                currentPrice = 70_000.0
            )
        )
    }

    @Test
    fun `price above but value is below current price returns false`() {
        assertEquals(
            false,
            useCase(
                type = AlertType.PRICE,
                direction = AlertDirection.ABOVE,
                value = "50000",
                currentPrice = 70_000.0
            )
        )
    }

    @Test
    fun `price below but value is above current price returns false`() {
        assertEquals(
            false,
            useCase(
                type = AlertType.PRICE,
                direction = AlertDirection.BELOW,
                value = "90000",
                currentPrice = 70_000.0
            )
        )
    }

    @Test
    fun `price type without currentPrice returns false`() {
        assertEquals(
            false,
            useCase(
                type = AlertType.PRICE,
                direction = AlertDirection.ABOVE,
                value = "80000",
                currentPrice = null
            )
        )
    }

    @Test
    fun `price equal to current price above returns false`() {
        assertEquals(
            false,
            useCase(
                type = AlertType.PRICE,
                direction = AlertDirection.ABOVE,
                value = "70000",
                currentPrice = 70_000.0
            )
        )
    }

    @Test
    fun `price equal to current price below returns false`() {
        assertEquals(
            false,
            useCase(
                type = AlertType.PRICE,
                direction = AlertDirection.BELOW,
                value = "70000",
                currentPrice = 70_000.0
            )
        )
    }

    // ----- edit mode -----

    @Test
    fun `edit mode unchanged values returns false`() {
        val original = alert()
        assertEquals(
            false,
            useCase(
                type = original.type,
                direction = original.direction,
                value = original.targetValue.toBigDecimal().stripTrailingZeros().toPlainString(),
                originalAlert = original
            )
        )
    }

    @Test
    fun `edit mode changed value returns true`() {
        val original = alert()
        assertEquals(
            true,
            useCase(
                type = original.type,
                direction = original.direction,
                value = "10",
                originalAlert = original
            )
        )
    }

    @Test
    fun `edit mode changed type returns true`() {
        val original = alert()
        assertEquals(
            true,
            useCase(
                type = AlertType.PRICE,
                direction = original.direction,
                value = original.targetValue.toBigDecimal().stripTrailingZeros().toPlainString(),
                currentPrice = 1.0,
                originalAlert = original
            )
        )
    }

    @Test
    fun `edit mode changed direction returns true`() {
        val original = alert()
        assertEquals(
            true,
            useCase(
                type = original.type,
                direction = AlertDirection.BELOW,
                value = original.targetValue.toBigDecimal().stripTrailingZeros().toPlainString(),
                originalAlert = original
            )
        )
    }

    // ----- helpers -----

    private fun alert() = Alert(
        id = 1L,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PERCENT,
        direction = AlertDirection.ABOVE,
        targetValue = 5.0
    )
}
