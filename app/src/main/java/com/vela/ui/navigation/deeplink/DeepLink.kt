package com.vela.ui.navigation.deeplink

sealed interface DeepLink {
    data class CoinDetail(
        val coinId: String,
        val alertId: Long? = null
    ) : DeepLink
}
