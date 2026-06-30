package com.vela.ui.screens.alerts

import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertLabelFormatterTest {
    private val formatter = AlertLabelFormatter()

    // ----- PRICE -----

    @Test
    fun `price above whole number`() {
        assertEquals(
            "Price above \$70,000",
            formatter.format(AlertDirection.ABOVE, 70_000.0, AlertType.PRICE)
        )
    }

    @Test
    fun `price below whole number`() {
        assertEquals(
            "Price below \$50,000",
            formatter.format(AlertDirection.BELOW, 50_000.0, AlertType.PRICE)
        )
    }

    @Test
    fun `price above decimal strips trailing zeros`() {
        assertEquals(
            "Price above \$70,000.5",
            formatter.format(AlertDirection.ABOVE, 70_000.5, AlertType.PRICE)
        )
    }

    @Test
    fun `price below decimal strips trailing zeros`() {
        assertEquals(
            "Price below \$0.001",
            formatter.format(AlertDirection.BELOW, 0.001, AlertType.PRICE)
        )
    }

    @Test
    fun `price above large number uses comma separators`() {
        assertEquals(
            "Price above \$999,001",
            formatter.format(AlertDirection.ABOVE, 999_001.0, AlertType.PRICE)
        )
    }

    // ----- PERCENT -----

    @Test
    fun `percent above whole number`() {
        assertEquals(
            "Price change above 5%",
            formatter.format(AlertDirection.ABOVE, 5.0, AlertType.PERCENT)
        )
    }

    @Test
    fun `percent below whole number`() {
        assertEquals(
            "Price change below 10%",
            formatter.format(AlertDirection.BELOW, 10.0, AlertType.PERCENT)
        )
    }

    @Test
    fun `percent above decimal strips trailing zeros`() {
        assertEquals(
            "Price change above 2.5%",
            formatter.format(AlertDirection.ABOVE, 2.5, AlertType.PERCENT)
        )
    }
}
