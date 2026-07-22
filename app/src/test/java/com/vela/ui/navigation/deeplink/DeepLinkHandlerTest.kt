package com.vela.ui.navigation.deeplink

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeepLinkHandlerTest {

    private val handler = DeepLinkHandler()

    @Test
    fun `emit delivers deeplink to flow collector`() = runTest {
        val deepLink = DeepLink.CoinDetail(coinId = "bitcoin", alertId = 1L)

        handler.emit(deepLink)

        assertEquals(deepLink, handler.flow.first())
    }

    @Test
    fun `emit with no alertId delivers CoinDetail with null alertId`() = runTest {
        val deepLink = DeepLink.CoinDetail(coinId = "ethereum")

        handler.emit(deepLink)

        assertEquals(deepLink, handler.flow.first())
    }

    @Test
    fun `multiple emits are all received in order`() = runTest {
        val links = listOf(
            DeepLink.CoinDetail(coinId = "bitcoin"),
            DeepLink.CoinDetail(coinId = "ethereum", alertId = 2L)
        )

        links.forEach { handler.emit(it) }

        assertEquals(links, handler.flow.take(2).toList())
    }
}
