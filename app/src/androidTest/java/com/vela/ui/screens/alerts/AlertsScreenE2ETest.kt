package com.vela.ui.screens.alerts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.MainActivity
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.domain.usecase.EditAlertUseCase
import com.vela.ui.common.util.TestTags
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
class AlertsScreenE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var editAlert: EditAlertUseCase

    private val firstAlertLabel = "Price above \$999,001"
    private val secondAlertLabel = "Price below \$1"

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            editAlert(
                Alert(
                    id = 0L,
                    coinId = "bitcoin",
                    coinName = "Bitcoin",
                    coinSymbol = "btc",
                    type = AlertType.PRICE,
                    direction = AlertDirection.ABOVE,
                    targetValue = 999_001.0
                )
            )
            editAlert(
                Alert(
                    id = 0L,
                    coinId = "bitcoin",
                    coinName = "Bitcoin",
                    coinSymbol = "btc",
                    type = AlertType.PRICE,
                    direction = AlertDirection.BELOW,
                    targetValue = 1.0
                )
            )
        }
    }

    private fun navigateToAlertsTab() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(hasText("Bitcoin"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(TestTags.navTab("alerts")).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(hasText(firstAlertLabel))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun alertsScreen_swipeToDelete_removesOnlyTargetedAlert() {
        navigateToAlertsTab()

        // Both seeded alerts are visible.
        composeRule.onNodeWithText(firstAlertLabel).assertIsDisplayed()
        composeRule.onNodeWithText(secondAlertLabel).assertIsDisplayed()

        // Swipe the first alert row left to reveal delete.
        composeRule.onNodeWithText(firstAlertLabel).performTouchInput { swipeLeft() }

        // Wait for the delete button to appear — only the swiped row reveals it.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule
                .onAllNodesWithContentDescription("Delete")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Tap the first (and only) revealed delete button.
        composeRule.onAllNodesWithContentDescription("Delete")[0].performClick()

        // First alert is gone, second alert still exists.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule
                .onAllNodes(hasText(firstAlertLabel))
                .fetchSemanticsNodes()
                .isEmpty()
        }
        composeRule.onNodeWithText(firstAlertLabel).assertDoesNotExist()
        composeRule.onNodeWithText(secondAlertLabel).assertIsDisplayed()
    }
}
