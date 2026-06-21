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
import com.vela.ui.common.util.TestTags
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed interface Screen {
    @Serializable
    data object Dashboard : Screen

    @Serializable
    data class CoinDetail(val coinId: String) : Screen
}

@Serializable
sealed class DashboardTab(
    @StringRes val labelRes: Int,
    @Transient val icon: ImageVector = Icons.Default.Home,
    @Transient val testTag: String = TestTags.navTab("home")
) {
    companion object {
        @Transient
        val tabs: List<DashboardTab> = listOf(Home, Markets, Watchlist, Search, Alerts)
    }

    @Serializable
    data object Home : DashboardTab(R.string.nav_home, Icons.Default.Home, TestTags.navTab("home"))

    @Serializable
    data object Markets : DashboardTab(R.string.nav_markets, Icons.Default.TrendingUp, TestTags.navTab("markets"))

    @Serializable
    data object Watchlist : DashboardTab(R.string.nav_watchlist, Icons.Default.Star, TestTags.navTab("watchlist"))

    @Serializable
    data object Search : DashboardTab(R.string.nav_search, Icons.Default.Search, TestTags.navTab("search"))

    @Serializable
    data object Alerts : DashboardTab(R.string.nav_alerts, Icons.Default.Notifications, TestTags.navTab("alerts"))
}
