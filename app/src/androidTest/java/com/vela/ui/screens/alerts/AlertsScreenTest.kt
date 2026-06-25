package com.vela.ui.screens.alerts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.ui.common.util.SharedTransitionWrapper
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val bitcoinGroup = AlertGroupUiModel(
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        coinImage = null,
        alerts = listOf(
            AlertRowUiModel(id = 1L, label = "Price above \$80,000", isTriggered = false),
            AlertRowUiModel(id = 2L, label = "Price below \$60,000", isTriggered = true)
        )
    )

    private val ethereumGroup = AlertGroupUiModel(
        coinId = "ethereum",
        coinName = "Ethereum",
        coinSymbol = "eth",
        coinImage = null,
        alerts = listOf(
            AlertRowUiModel(id = 3L, label = "Price change above 5%", isTriggered = false)
        )
    )

    private fun setScreen(
        uiState: AlertsUiState,
        onAlertClick: (String, Long?) -> Unit = { _, _ -> }
    ) {
        composeRule.setContent {
            SharedTransitionWrapper {
                AlertsScreen(uiState = uiState, onAlertClick = onAlertClick)
            }
        }
    }

    // ----- loading -----

    @Test
    fun loadingState_showsNoContent() {
        setScreen(AlertsUiState.Loading)
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
    }

    // ----- empty -----

    @Test
    fun emptyState_showsEmptyTitle() {
        setScreen(AlertsUiState.Empty)
        composeRule.onNodeWithText("No alerts yet").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptySubtitle() {
        setScreen(AlertsUiState.Empty)
        composeRule.onNodeWithText("Go to a coin\'s detail screen and tap the Alerts tab to add one").assertIsDisplayed()
    }

    // ----- success -----

    @Test
    fun successState_showsCoinName() {
        setScreen(AlertsUiState.Success(groups = listOf(bitcoinGroup)))
        composeRule.onNodeWithText("Bitcoin · BTC").assertIsDisplayed()
    }

    @Test
    fun successState_showsAlertLabels() {
        setScreen(AlertsUiState.Success(groups = listOf(bitcoinGroup)))
        composeRule.onNodeWithText("Price above \$80,000").assertIsDisplayed()
        composeRule.onNodeWithText("Price below \$60,000").assertIsDisplayed()
    }

    @Test
    fun successState_showsMultipleGroups() {
        setScreen(AlertsUiState.Success(groups = listOf(bitcoinGroup, ethereumGroup)))
        composeRule.onNodeWithText("Bitcoin · BTC").assertIsDisplayed()
        composeRule.onNodeWithText("Ethereum · ETH").assertIsDisplayed()
    }

    @Test
    fun successState_triggeredAlert_showsTriggeredBadge() {
        setScreen(AlertsUiState.Success(groups = listOf(bitcoinGroup)))
        composeRule.onNodeWithText("Triggered").assertIsDisplayed()
    }

    // ----- callbacks -----

    @Test
    fun tapAlertRow_invokesOnAlertClickWithAlertId() {
        var clickedCoinId: String? = null
        var clickedAlertId: Long? = null
        setScreen(
            uiState = AlertsUiState.Success(groups = listOf(bitcoinGroup)),
            onAlertClick = { coinId, alertId ->
                clickedCoinId = coinId
                clickedAlertId = alertId
            }
        )
        composeRule.onNodeWithText("Price above \$80,000").performClick()
        assertEquals("bitcoin", clickedCoinId)
        assertEquals(1L, clickedAlertId)
    }

    @Test
    fun tapHeaderRow_invokesOnAlertClickWithNullAlertId() {
        var clickedCoinId: String? = null
        var clickedAlertId: Long? = -1L
        setScreen(
            uiState = AlertsUiState.Success(groups = listOf(bitcoinGroup)),
            onAlertClick = { coinId, alertId ->
                clickedCoinId = coinId
                clickedAlertId = alertId
            }
        )
        composeRule.onNodeWithText("Bitcoin · BTC").performClick()
        assertEquals("bitcoin", clickedCoinId)
        assertEquals(null, clickedAlertId)
    }
}
