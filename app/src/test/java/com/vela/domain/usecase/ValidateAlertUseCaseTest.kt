package com.vela.domain.usecase

import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateAlertUseCaseTest {
    private val useCase = ValidateAlertUseCase()

    private fun baseAlert() = Alert(
        id = 1L,
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        targetValue = 70_000.0
    )

    private fun assertValid(
        type: AlertType? = AlertType.PRICE,
        direction: AlertDirection? = AlertDirection.ABOVE,
        value: String = "80000",
        currentPrice: Double? = 70_000.0,
        originalAlert: Alert? = null,
        expected: Boolean
    ) {
        assertEquals(
            expected,
            useCase(
                type = type,
                direction = direction,
                value = value,
                currentPrice = currentPrice,
                originalAlert = originalAlert
            )
        )
    }

    // ----- base validation -----

    @Test
    fun `type null returns false`() = assertValid(type = null, expected = false)

    @Test
    fun `direction null returns false`() = assertValid(direction = null, expected = false)

    @Test
    fun `non-parseable value returns false`() = assertValid(value = "abc", expected = false)

    // ----- PRICE type -----

    @Test
    fun `PRICE ABOVE value greater than price returns true`() = assertValid(type = AlertType.PRICE, direction = AlertDirection.ABOVE, value = "80000", currentPrice = 70_000.0, expected = true)

    @Test
    fun `PRICE ABOVE value less than price returns false`() = assertValid(type = AlertType.PRICE, direction = AlertDirection.ABOVE, value = "60000", currentPrice = 70_000.0, expected = false)

    @Test
    fun `PRICE BELOW value less than price returns true`() = assertValid(type = AlertType.PRICE, direction = AlertDirection.BELOW, value = "60000", currentPrice = 70_000.0, expected = true)

    @Test
    fun `PRICE BELOW value greater than price returns false`() = assertValid(type = AlertType.PRICE, direction = AlertDirection.BELOW, value = "80000", currentPrice = 70_000.0, expected = false)

    @Test
    fun `PRICE with null currentPrice returns false`() = assertValid(type = AlertType.PRICE, currentPrice = null, expected = false)

    // ----- PERCENT type -----

    @Test
    fun `PERCENT ABOVE with valid value returns true regardless of price`() = assertValid(type = AlertType.PERCENT, direction = AlertDirection.ABOVE, value = "5", currentPrice = null, expected = true)

    @Test
    fun `PERCENT BELOW with valid value returns true regardless of price`() = assertValid(type = AlertType.PERCENT, direction = AlertDirection.BELOW, value = "3", currentPrice = null, expected = true)

    // ----- edit mode change detection -----

    @Test
    fun `edit mode nothing changed returns false`() = assertValid(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "70000",
        currentPrice = 60_000.0,
        originalAlert = baseAlert(),
        expected = false
    )

    @Test
    fun `edit mode type changed returns true`() = assertValid(
        type = AlertType.PERCENT,
        direction = AlertDirection.ABOVE,
        value = "5",
        currentPrice = null,
        originalAlert = baseAlert(),
        expected = true
    )

    @Test
    fun `edit mode direction changed returns true`() = assertValid(
        type = AlertType.PRICE,
        direction = AlertDirection.BELOW,
        value = "60000",
        currentPrice = 70_000.0,
        originalAlert = baseAlert(),
        expected = true
    )

    @Test
    fun `edit mode value changed returns true`() = assertValid(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "80000",
        currentPrice = 60_000.0,
        originalAlert = baseAlert(),
        expected = true
    )

    @Test
    fun `edit mode all changed back to original returns false`() {
        val original = baseAlert()
        assertValid(
            type = original.type,
            direction = original.direction,
            value = original.targetValue
                .toBigDecimal()
                .stripTrailingZeros()
                .toPlainString(),
            currentPrice = 60_000.0,
            originalAlert = original,
            expected = false
        )
    }
}
