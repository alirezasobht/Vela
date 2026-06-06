package com.vela.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vela.ui.common.components.PlaceholderScreen
import com.vela.ui.screens.detail.DetailRoute
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
            HomeRoute(
                navigateToDetail = { coinId ->
                    navController.navigate(DetailDestination(coinId))
                }
            )
        }
        composable(Screen.Markets.route) {
            MarketsRoute(
                navigateToDetail = { coinId ->
                    navController.navigate(DetailDestination(coinId))
                }
            )
        }
        composable(Screen.Watchlist.route) {
            PlaceholderScreen(title = "Watchlist")
        }
        composable(Screen.Search.route) {
            SearchRoute(
                navigateToDetail = { coinId ->
                    navController.navigate(DetailDestination(coinId))
                }
            )
        }
        composable(Screen.Alerts.route) {
            PlaceholderScreen(title = "Alerts")
        }
        composable<DetailDestination> { backStackEntry ->
            val dest: DetailDestination = backStackEntry.toRoute()
            DetailRoute(
                coinId = dest.coinId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}