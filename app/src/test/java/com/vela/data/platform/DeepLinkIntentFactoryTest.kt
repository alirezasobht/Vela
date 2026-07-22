package com.vela.data.platform

import android.content.Context
import android.content.Intent
import com.vela.ui.navigation.deeplink.DeepLink
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkIntentFactoryTest {

    private val context = mockk<Context>()
    private val factory = DeepLinkIntentFactory(context)

    @Test
    fun `fromIntent returns CoinDetail with coinId and alertId`() {
        val intent = intentWith(type = "coin_detail", coinId = "bitcoin", alertId = 42L)

        assertEquals(DeepLink.CoinDetail(coinId = "bitcoin", alertId = 42L), factory.fromIntent(intent))
    }

    @Test
    fun `fromIntent returns CoinDetail with null alertId when extra is absent`() {
        val intent = intentWith(type = "coin_detail", coinId = "bitcoin", alertId = -1L)

        assertEquals(DeepLink.CoinDetail(coinId = "bitcoin", alertId = null), factory.fromIntent(intent))
    }

    @Test
    fun `fromIntent returns null when type extra is missing`() {
        val intent = intentWith(type = null, coinId = "bitcoin", alertId = -1L)

        assertNull(factory.fromIntent(intent))
    }

    @Test
    fun `fromIntent returns null when coinId extra is missing`() {
        val intent = intentWith(type = "coin_detail", coinId = null, alertId = -1L)

        assertNull(factory.fromIntent(intent))
    }

    @Test
    fun `fromIntent returns null for unknown type`() {
        val intent = intentWith(type = "unknown_type", coinId = "bitcoin", alertId = -1L)

        assertNull(factory.fromIntent(intent))
    }

    // ----- helpers -----

    private fun intentWith(
        type: String?,
        coinId: String?,
        alertId: Long
    ): Intent = mockk<Intent>().also {
        every { it.getStringExtra("vela_deeplink_type") } returns type
        every { it.getStringExtra("vela_deeplink_coin_id") } returns coinId
        every { it.getLongExtra("vela_deeplink_alert_id", -1L) } returns alertId
    }
}
