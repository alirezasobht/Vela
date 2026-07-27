package com.vela.data.pricepolling

import org.junit.Assert.assertEquals
import org.junit.Test

class PricePollingConfigImplTest {

    private val config = PricePollingConfigImpl()

    @Test
    fun `refreshDelaySeconds returns expected default value`() {
        assertEquals(300L, config.refreshDelaySeconds)
    }
}
