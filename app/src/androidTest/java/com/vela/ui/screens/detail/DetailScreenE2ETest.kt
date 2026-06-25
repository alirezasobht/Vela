package com.vela.ui.screens.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
class DetailScreenE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToDetail() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(androidx.compose.ui.test.hasText("Bitcoin"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    private fun openAlertForm() {
        composeRule.onNodeWithText("Alerts").performClick()
        composeRule.onNodeWithText("Add Alert").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule
                .onAllNodes(androidx.compose.ui.test.hasText("Cancel"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    // 1. Navigate to detail → tap Add Alert → form chips visible
    @Test
    fun detailScreen_tapAddAlert_showsAlertForm() {
        navigateToDetail()
        openAlertForm()
        composeRule.onNodeWithText("% Change").assertIsDisplayed()
        composeRule.onNodeWithText("Above").assertIsDisplayed()
        composeRule.onNodeWithText("Below").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    // 2. Alert form visible → tap Cancel → tabs visible again
    @Test
    fun alertForm_tapCancel_dismissesFormAndShowsTabs() {
        navigateToDetail()
        openAlertForm()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Stats").assertIsDisplayed()
        composeRule.onNodeWithText("Alerts").assertIsDisplayed()
    }

    // 3. Fill valid PERCENT values → Add Alert button enabled
    @Test
    fun alertForm_fillValidPercentValues_enablesSubmitButton() {
        navigateToDetail()
        openAlertForm()
        composeRule.onNodeWithText("% Change").performClick()
        composeRule.onNodeWithText("Above").performClick()
        composeRule
            .onNode(androidx.compose.ui.test.hasSetTextAction())
            .performTextClearance()
        composeRule
            .onNode(androidx.compose.ui.test.hasSetTextAction())
            .performTextInput("5")
        composeRule.onNodeWithText("Add Alert").assertIsEnabled()
    }
}
