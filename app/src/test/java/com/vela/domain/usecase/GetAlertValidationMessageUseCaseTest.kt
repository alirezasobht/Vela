package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAlertValidationMessageUseCaseTest {
    private val useCase = GetAlertValidationMessageUseCase()

    // ----- incomplete fields -----

    @Test
    fun `returns Invalid select type when type is null`() {
        assertEquals(
            AlertValidationResult.Invalid("Select a type"),
            useCase(type = null, direction = AlertDirection.ABOVE, value = "5")
        )
    }

    @Test
    fun `returns Invalid select direction when direction is null`() {
        assertEquals(
            AlertValidationResult.Invalid("Select a direction"),
            useCase(type = AlertType.PERCENT, direction = null, value = "5")
        )
    }

    @Test
    fun `returns Invalid enter value when value is blank`() {
        assertEquals(
            AlertValidationResult.Invalid("Enter a target value"),
            useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "")
        )
    }

    @Test
    fun `returns Invalid valid number when value is not a number`() {
        assertEquals(
            AlertValidationResult.Invalid("Enter a valid number"),
            useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "abc")
        )
    }

    // ----- percent type -----

    @Test
    fun `percent above valid value returns Valid`() {
        assertEquals(
            AlertValidationResult.Valid,
            useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "5")
        )
    }

    @Test
    fun `percent below valid value returns Valid`() {
        assertEquals(
            AlertValidationResult.Valid,
            useCase(type = AlertType.PERCENT, direction = AlertDirection.BELOW, value = "10")
        )
    }

    @Test
    fun `percent with decimal returns Valid`() {
        assertEquals(
            AlertValidationResult.Valid,
            useCase(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "2.5")
        )
    }

    // ----- price type -----

    @Test
    fun `price above current price returns Valid`() {
        assertEquals(
            AlertValidationResult.Valid,
            useCase(type = AlertType.PRICE, direction = AlertDirection.ABOVE, value = "80000", currentPrice = 70_000.0)
        )
    }

    @Test
    fun `price below current price returns Valid`() {
        assertEquals(
            AlertValidationResult.Valid,
            useCase(type = AlertType.PRICE, direction = AlertDirection.BELOW, value = "60000", currentPrice = 70_000.0)
        )
    }

    @Test
    fun `price above but value below current price returns Invalid`() {
        assertEquals(
            AlertValidationResult.Invalid("Target must be above current price"),
            useCase(type = AlertType.PRICE, direction = AlertDirection.ABOVE, value = "50000", currentPrice = 70_000.0)
        )
    }

    @Test
    fun `price below but value above current price returns Invalid`() {
        assertEquals(
            AlertValidationResult.Invalid("Target must be below current price"),
            useCase(type = AlertType.PRICE, direction = AlertDirection.BELOW, value = "90000", currentPrice = 70_000.0)
        )
    }

    @Test
    fun `price type without currentPrice returns Invalid unavailable`() {
        assertEquals(
            AlertValidationResult.Invalid("Current price unavailable"),
            useCase(type = AlertType.PRICE, direction = AlertDirection.ABOVE, value = "80000", currentPrice = null)
        )
    }

    @Test
    fun `price equal to current price above returns Invalid`() {
        assertEquals(
            AlertValidationResult.Invalid("Target must be above current price"),
            useCase(type = AlertType.PRICE, direction = AlertDirection.ABOVE, value = "70000", currentPrice = 70_000.0)
        )
    }

    @Test
    fun `price equal to current price below returns Invalid`() {
        assertEquals(
            AlertValidationResult.Invalid("Target must be below current price"),
            useCase(type = AlertType.PRICE, direction = AlertDirection.BELOW, value = "70000", currentPrice = 70_000.0)
        )
    }

    // ----- edit mode -----

    @Test
    fun `edit mode unchanged values returns Unchanged`() {
        val original = alert()
        assertEquals(
            AlertValidationResult.Unchanged,
            useCase(
                type = original.type,
                direction = original.direction,
                value = original.targetValue.toBigDecimal().stripTrailingZeros().toPlainString(),
                originalAlert = original
            )
        )
    }

    @Test
    fun `edit mode changed value returns Valid`() {
        val original = alert()
        assertEquals(
            AlertValidationResult.Valid,
            useCase(
                type = original.type,
                direction = original.direction,
                value = "10",
                originalAlert = original
            )
        )
    }

    @Test
    fun `edit mode changed type returns Valid`() {
        val original = alert()
        assertEquals(
            AlertValidationResult.Valid,
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
    fun `edit mode changed direction returns Valid`() {
        val original = alert()
        assertEquals(
            AlertValidationResult.Valid,
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
