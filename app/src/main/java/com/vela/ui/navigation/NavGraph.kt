package com.vela.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vela.ui.common.components.PlaceholderScreen
import com.vela.ui.common.util.LocalAnimatedVisibilityScope
import com.vela.ui.common.util.LocalSharedTransitionScope
import com.vela.ui.screens.detail.DetailRoute
import com.vela.ui.screens.home.HomeRoute
import com.vela.ui.screens.markets.MarketsRoute
import com.vela.ui.screens.search.SearchRoute
import com.vela.ui.screens.watchlist.WatchlistRoute

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RootNavGraph(
    rootNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = rootNavController,
                startDestination = Screen.Dashboard,
            ) {
                composable<Screen.Dashboard> {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                        DashboardScaffold { innerPadding, bottomNavController ->
                            DashboardNavGraph(
                                rootNavController = rootNavController,
                                bottomNavController = bottomNavController,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }

                composable<Screen.CoinDetail> { backStackEntry ->
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                        val dest: Screen.CoinDetail = backStackEntry.toRoute()
                        DetailRoute(
                            onBack = { rootNavController.popBackStack() }
                        )
                    }
                }
            }
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
            WatchlistRoute(
                navigateToDetail = { coinId ->
                    rootNavController.navigate(Screen.CoinDetail(coinId))
                }
            )
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