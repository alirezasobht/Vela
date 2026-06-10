package com.vela.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.MainActivity
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
        composeRule.onNodeWithText("Markets").assertIsDisplayed()
    }

    @Test
    fun homeScreen_tapCoin_navigatesToDetail() {
        // Wait for assets to load then tap Bitcoin
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Bitcoin")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        // Back button only exists on detail screen
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun detailScreen_backButton_returnsToHome() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Bitcoin")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithText("Markets").assertIsDisplayed()
    }

    @Test
    fun bottomNav_tapMarkets_navigatesToMarketsTab() {
        composeRule.onNodeWithText("Markets").performClick()
        // Markets tab shows filter chip with "All" category
        composeRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun bottomNav_tapSearch_navigatesToSearchTab() {
        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("Start typing to search").assertIsDisplayed()
    }

    @Test
    fun searchScreen_tapCoin_navigatesToDetail() {
        composeRule.onNodeWithText("Search").performClick()
        // Search results are pre-populated via FakeSearchRepository
        // Type a query to trigger search
        composeRule.onNodeWithText("Search coins…").performClick()
        composeRule
            .onNode(androidx.compose.ui.test.hasSetTextAction())
            .performTextInput("bitcoin")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Bitcoin")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun bottomNav_tabSwitching_preservesBackStack() {
        // Go to Search
        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("Start typing to search").assertIsDisplayed()
        // Go to Markets
        composeRule.onNodeWithText("Markets").performClick()
        composeRule.onNodeWithText("All").assertIsDisplayed()
        // Back to Home
        composeRule.onNodeWithText("Home").performClick()
        composeRule.onNodeWithText("Markets").assertIsDisplayed()
    }
}