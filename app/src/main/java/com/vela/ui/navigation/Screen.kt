package com.vela.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.vela.R

sealed class Screen(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    data object Markets : Screen("markets", R.string.nav_markets, Icons.Default.TrendingUp)
    data object Watchlist : Screen("watchlist", R.string.nav_watchlist, Icons.Default.Star)
    data object Search : Screen("search", R.string.nav_search, Icons.Default.Search)
    data object Alerts : Screen("alerts", R.string.nav_alerts, Icons.Default.Notifications)
}