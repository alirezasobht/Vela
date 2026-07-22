package com.vela.ui.navigation.deeplink

import androidx.navigation.NavController
import com.vela.ui.navigation.Screen
import com.vela.ui.screens.detail.state.DetailTab

fun navigate(
    navController: NavController,
    deepLink: DeepLink
) {
    navController.navigate(deepLink.toScreen())
}

private fun DeepLink.toScreen(): Screen = when (this) {
    is DeepLink.CoinDetail -> Screen.CoinDetail(
        coinId = coinId,
        initialTab = DetailTab.ALERTS,
        initialAlertId = alertId
    )
}
