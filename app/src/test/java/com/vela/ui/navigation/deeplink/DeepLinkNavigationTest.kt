package com.vela.ui.navigation.deeplink

import androidx.navigation.NavController
import com.vela.ui.navigation.Screen
import com.vela.ui.screens.detail.state.DetailTab
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeepLinkNavigationTest {

    private val navController = mockk<NavController>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic("androidx.navigation.NavControllerKt")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `CoinDetail navigates to Screen CoinDetail with ALERTS tab`() {
        navigate(navController, DeepLink.CoinDetail(coinId = "bitcoin", alertId = 42L))

        verify {
            navController.navigate(
                Screen.CoinDetail(coinId = "bitcoin", initialTab = DetailTab.ALERTS, initialAlertId = 42L)
            )
        }
    }

    @Test
    fun `CoinDetail with no alertId passes null initialAlertId`() {
        navigate(navController, DeepLink.CoinDetail(coinId = "ethereum"))

        verify {
            navController.navigate(
                Screen.CoinDetail(coinId = "ethereum", initialTab = DetailTab.ALERTS, initialAlertId = null)
            )
        }
    }
}
