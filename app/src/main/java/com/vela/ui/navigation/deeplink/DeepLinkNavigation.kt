package com.vela.ui.navigation.deeplink

import androidx.navigation.NavController
import com.vela.ui.navigation.Screen
import com.vela.ui.screens.detail.state.DetailTab

fun NavController.navigate(deepLink: DeepLink) {
    when (deepLink) {
        is DeepLink.CoinDetail -> navigate(
            Screen.CoinDetail(
                coinId = deepLink.coinId,
                initialTab = DetailTab.ALERTS,
                initialAlertId = deepLink.alertId
            )
        )
    }
}
