package com.vela.ui.navigation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.MainActivity
import com.vela.ui.common.util.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appLaunches_homeScreenVisible() {
        // Home tab is the start destination — Markets title should be visible
        composeRule.onAllNodesWithText("Markets").assertCountEquals(2)
    }

    @Test
    fun homeScreen_tapCoin_navigatesToDetail() {
        // Wait for assets to load then tap Bitcoin
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Bitcoin")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        // Back button only exists on detail screen
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun detailScreen_backButton_returnsToHome() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Bitcoin")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onAllNodesWithText("Markets").assertCountEquals(2)
    }

    @Test
    fun bottomNav_tapMarkets_navigatesToMarketsTab() {
        composeRule.onNodeWithTag(TestTags.navTab("markets")).performClick()
        // Markets tab shows filter chip with "All" category
        composeRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun bottomNav_tapSearch_navigatesToSearchTab() {
        composeRule.onNodeWithTag(TestTags.navTab("search")).performClick()
        composeRule.onNodeWithText("Start typing to search").assertIsDisplayed()
    }

    @Test
    fun bottomNav_tapWatchlist_navigatesToWatchlistTab() {
        composeRule.onNodeWithTag(TestTags.navTab("watchlist")).performClick()
        composeRule.onAllNodesWithText("Watchlist").assertCountEquals(2)
    }

    @Test
    fun searchScreen_tapCoin_navigatesToDetail() {
        composeRule.onNodeWithTag(TestTags.navTab("search")).performClick()
        // Search results are pre-populated via FakeSearchRepository
        // Type a query to trigger search
        composeRule.onNodeWithText("Search coins…").performClick()
        composeRule
            .onNode(
                androidx.compose.ui.test
                    .hasSetTextAction()
            ).performTextInput("bitcoin")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Bitcoin")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        // Go back to Search
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithText("Search").assertIsDisplayed()
    }

    @Test
    fun bottomNav_tabSwitching_preservesBackStack() {
        // Land on Home
        composeRule.onAllNodesWithText("Markets").assertCountEquals(2)
        // Go to Search
        composeRule.onNodeWithTag(TestTags.navTab("search")).performClick()
        composeRule.onNodeWithText("Start typing to search").assertIsDisplayed()
        // Go to Markets
        composeRule.onNodeWithTag(TestTags.navTab("markets")).performClick()
        composeRule.onNodeWithText("All").assertIsDisplayed()
        // Go to Watchlist
        composeRule.onAllNodesWithText("Watchlist").assertCountEquals(1)
        composeRule.onNodeWithTag(TestTags.navTab("watchlist")).performClick()
        composeRule.onAllNodesWithText("Watchlist").assertCountEquals(2)
        // Back to Home
        composeRule.onNodeWithTag(TestTags.navTab("home")).performClick()
        composeRule.onAllNodesWithText("Markets").assertCountEquals(2)
    }

    @Test
    fun candleStickChart_tapFullScreen_navigateToFullScreenChart() {
        // go to Detail
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Bitcoin")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()

        // Open fullscreen chart
        composeRule.onNodeWithContentDescription("Open fullscreen chart").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close fullscreen chart").assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription("Open fullscreen chart").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasContentDescription("Close fullscreen chart")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Close fullscreen chart
        composeRule.onNodeWithContentDescription("Open fullscreen chart").assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription("Close fullscreen chart").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close fullscreen chart").performClick()

        // Verify we are back to detail screen
        composeRule.onNodeWithContentDescription("Open fullscreen chart").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close fullscreen chart").assertIsNotDisplayed()
    }

    // ----- Watchlist -----

    @Test
    fun bottomNav_tapWatchlist_showsPrePopulatedAssets() {
        composeRule.onNodeWithTag(TestTags.navTab("watchlist")).performClick()

        // FakeWatchlistRepository pre-populates with the first 3 fake assets
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Bitcoin")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("Ethereum").assertIsDisplayed()
        composeRule.onNodeWithText("Tether").assertIsDisplayed()
    }

    @Test
    fun watchlistTab_tapCoin_navigatesToDetail() {
        composeRule.onNodeWithTag(TestTags.navTab("watchlist")).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Bitcoin")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun toggleStarOnHome_removesAssetFromWatchlistTab() {
        // Wait for Home assets to load
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Bitcoin")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Bitcoin is pre-watchlisted — tap its star to remove it
        composeRule.onNodeWithTag(TestTags.watchlistStar("bitcoin")).performClick()

        // Switch to Watchlist tab
        composeRule.onNodeWithTag(TestTags.navTab("watchlist")).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Ethereum")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Bitcoin should no longer be in the watchlist
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
        composeRule.onNodeWithText("Ethereum").assertIsDisplayed()
    }

    @Test
    fun toggleStarOnDetail_addsAssetToWatchlistTab() {
        // Navigate to a non-watchlisted coin's detail (Solana)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Solana")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Solana").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Tap the star in the detail header to add it to the watchlist
        composeRule.onNodeWithContentDescription("Watchlist").performClick()

        // Go back and switch to Watchlist tab
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithTag(TestTags.navTab("watchlist")).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Solana")
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Solana").assertIsDisplayed()
    }
}
