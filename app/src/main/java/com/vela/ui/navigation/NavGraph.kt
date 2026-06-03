package com.vela.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
                navigateToDetail = { coinId: String ->
                    navController.navigate(DetailDestination.createRoute(coinId))
                }
            )
        }
        composable(Screen.Markets.route) {
            MarketsRoute(
                navigateToDetail = { coinId: String ->
                    navController.navigate(DetailDestination.createRoute(coinId))
                }
            )
        }
        composable(Screen.Watchlist.route) {
            PlaceholderScreen(title = "Watchlist")
        }
        composable(Screen.Search.route) {
            SearchRoute(
                navigateToDetail = { coinId: String ->
                    navController.navigate(DetailDestination.createRoute(coinId))
                }
            )
        }
        composable(Screen.Alerts.route) {
            PlaceholderScreen(title = "Alerts")
        }
        composable(
            route = DetailDestination.ROUTE,
            arguments = listOf(
                navArgument(DetailDestination.ARG_COIN_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString(DetailDestination.ARG_COIN_ID)
                ?: return@composable
            DetailRoute(
                onBack = { navController.popBackStack() }
            )
        }
    }
}