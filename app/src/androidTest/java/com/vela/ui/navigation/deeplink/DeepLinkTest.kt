package com.vela.ui.navigation.deeplink

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.MainActivity
import com.vela.data.repository.fake.FakeAlertRepository
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.ui.base.AssetPreviewCache
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeepLinkTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var assetPreviewCache: AssetPreviewCache

    @Inject
    lateinit var fakeAlertRepository: FakeAlertRepository

    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler

    @Before
    fun setup() {
        hiltRule.inject()

        // Seed cache so detail screen can show initial data
        assetPreviewCache.put(FakeAssetDataSource.assets)

        // Wait for home to be ready before delivering deeplink
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("Bitcoin")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun coinDetailDeeplink_navigatesToDetailAlertsTab() {
        composeRule.runOnUiThread {
            deepLinkHandler.emit(DeepLink.CoinDetail("bitcoin"))
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("Add Alert")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithText("Add Alert").assertIsDisplayed()
    }

    @Test
    fun coinDetailDeeplink_showsCoinPage() {
        composeRule.runOnUiThread {
            deepLinkHandler.emit(DeepLink.CoinDetail("bitcoin"))
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasContentDescription("Back")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun coinDetailDeeplink_withAlertId_opensAlertEditFormWithDetails() {
        val alertId = runBlocking {
            fakeAlertRepository.createAlert(
                Alert(
                    id = 0L,
                    coinId = "bitcoin",
                    coinName = "Bitcoin",
                    coinSymbol = "btc",
                    type = AlertType.PRICE,
                    direction = AlertDirection.ABOVE,
                    targetValue = 80_000.0
                )
            )
        }

        composeRule.runOnUiThread {
            deepLinkHandler.emit(DeepLink.CoinDetail("bitcoin", alertId = alertId))
        }

        // observeAlerts() emits first, then openAlertForm() is called → form pre-populated
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("Cancel")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeRule.onNodeWithText("80000").assertIsDisplayed() // pre-populated value
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun multipleDifferentDeeplinks_alwaysNavigatesToLatest() {
        // 1. Navigate to Bitcoin Detail
        composeRule.runOnUiThread {
            deepLinkHandler.emit(DeepLink.CoinDetail("bitcoin"))
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("Add Alert")).fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Go back to Home
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("Bitcoin")).fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Navigate to Ethereum Detail
        composeRule.runOnUiThread {
            deepLinkHandler.emit(DeepLink.CoinDetail("ethereum"))
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("Ethereum")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithText("Ethereum").assertIsDisplayed()
    }
}
