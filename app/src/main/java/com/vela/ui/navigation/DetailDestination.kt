package com.vela.ui.navigation

object DetailDestination {
    const val ROUTE = "detail/{coinId}"
    const val ARG_COIN_ID = "coinId"
    fun createRoute(coinId: String) = "detail/$coinId"
}