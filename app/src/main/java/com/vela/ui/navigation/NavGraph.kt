package com.vela.ui.navigation

import androidx.compose.foundation.layout.padding
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
fun RootNavGraph(
    rootNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = rootNavController,
        startDestination = Screen.Dashboard,
        modifier = modifier
    ) {

        composable<Screen.Dashboard> {
            DashboardScaffold { innerPadding, bottomNavController ->
                DashboardNavGraph(
                    rootNavController = rootNavController,
                    bottomNavController = bottomNavController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable<Screen.CoinDetail> { backStackEntry ->
            val dest: Screen.CoinDetail = backStackEntry.toRoute()
            DetailRoute(
                coinId = dest.coinId,
                onBack = { rootNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun DashboardNavGraph(
    rootNavController: NavHostController,
    bottomNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = bottomNavController,
        startDestination = DashboardTab.Home,
        modifier = modifier
    ) {

        composable<DashboardTab.Home> {
            HomeRoute(
                navigateToDetail = { coinId ->
                    rootNavController.navigate(Screen.CoinDetail(coinId))
                }
            )
        }
        composable<DashboardTab.Markets> {
            MarketsRoute(
                navigateToDetail = { coinId ->
                    rootNavController.navigate(Screen.CoinDetail(coinId))
                }
            )
        }
        composable<DashboardTab.Watchlist> {
            PlaceholderScreen(title = "Watchlist")
        }
        composable<DashboardTab.Search> {
            SearchRoute(
                navigateToDetail = { coinId ->
                    rootNavController.navigate(Screen.CoinDetail(coinId))
                }
            )
        }
        composable<DashboardTab.Alerts> {
            PlaceholderScreen(title = "Alerts")
        }
    }
}