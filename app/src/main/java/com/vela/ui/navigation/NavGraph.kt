package com.vela.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vela.ui.common.components.PlaceholderScreen
import com.vela.ui.screens.home.HomeRoute
import com.vela.ui.screens.markets.MarketsRoute
import com.vela.ui.screens.search.SearchRoute

@Composable
fun VelaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeRoute()
        }
        composable(Screen.Markets.route) {
            MarketsRoute()
        }
        composable(Screen.Watchlist.route) {
            PlaceholderScreen(title = "Watchlist")
        }
        composable(Screen.Search.route) {
            SearchRoute()
        }
        composable(Screen.Alerts.route) {
            PlaceholderScreen(title = "Alerts")
        }
    }
}