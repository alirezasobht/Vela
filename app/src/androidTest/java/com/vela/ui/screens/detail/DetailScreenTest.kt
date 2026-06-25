package com.vela.ui.screens.detail

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.fake.FakeDetailDataSource
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.util.FullScreenOrientationController
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.screens.detail.state.AlertFormState
import com.vela.ui.screens.detail.state.DetailAction
import com.vela.ui.screens.detail.state.DetailContentState
import com.vela.ui.screens.detail.state.DetailScreenState
import com.vela.ui.screens.detail.state.DetailTab
import com.vela.ui.screens.detail.state.toUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalSharedTransitionApi::class)
class DetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val testOrientationController = FullScreenOrientationController { {} }

    private val successContent = DetailContentState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc
    )

    private fun defaultState(
        content: DetailContentState = successContent,
        isChartFullScreen: Boolean = false,
        alertFormState: AlertFormState = AlertFormState.Invisible,
        selectedTab: DetailTab = DetailTab.STATS
    ) = DetailScreenState(
        coinId = "bitcoin",
        isChartFullScreen = isChartFullScreen,
        isWatchlisted = false,
        alertFormState = alertFormState,
        content = if (content is DetailContentState.Success) content.copy(selectedTab = selectedTab) else content
    )

    private fun setDetailScreen(
        state: DetailScreenState = defaultState(),
        onAction: (DetailAction) -> Unit = {}
    ) {
        composeRule.setContent {
            SharedTransitionWrapper {
                DetailScreen(
                    state = state,
                    initialAsset = null,
                    onAction = onAction,
                    orientationController = testOrientationController
                )
            }
        }
    }

    // ----- loading / error -----

    @Test
    fun loadingState_showsNoContent() {
        setDetailScreen(state = defaultState(content = DetailContentState.Loading))
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
        composeRule.onNodeWithText("Try Again").assertDoesNotExist()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() {
        setDetailScreen(state = defaultState(content = DetailContentState.Error(AppError.NoInternet)))
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButtonInvokesCallback() {
        var retried = false
        setDetailScreen(
            state = defaultState(content = DetailContentState.Error(AppError.NoInternet)),
            onAction = { if (it == DetailAction.Retry) retried = true }
        )
        composeRule.onNodeWithText("Try Again").performClick()
        assertEquals(true, retried)
    }

    // ----- success -----

    @Test
    fun successState_showsCoinPrice() {
        setDetailScreen()
        composeRule.onNodeWithText("\$67,420").assertIsDisplayed()
    }

    @Test
    fun successState_showsTimeRangeChips() {
        setDetailScreen()
        composeRule.onNodeWithText("1D").assertIsDisplayed()
        composeRule.onNodeWithText("7D").assertIsDisplayed()
        composeRule.onNodeWithText("1M").assertIsDisplayed()
        composeRule.onNodeWithText("3M").assertIsDisplayed()
        composeRule.onNodeWithText("1Y").assertIsDisplayed()
    }

    @Test
    fun successState_showsMarketCapRankBadge() {
        setDetailScreen()
        composeRule.onNodeWithText("#27").assertIsDisplayed()
    }

    @Test
    fun successState_marketCapRankBadge_hiddenWhenNull() {
        val detailNoRank = FakeDetailDataSource.detail.copy(marketCapRank = null).toUiModel()
        setDetailScreen(
            state = defaultState(
                content = DetailContentState.Success(detail = detailNoRank, isChartLoading = false)
            )
        )
        composeRule.onNodeWithText("#", substring = true).assertDoesNotExist()
    }

    @Test
    fun successState_nonBlockingErrorBannerVisible() {
        setDetailScreen(
            state = defaultState(content = successContent.copy(nonBlockingError = AppError.NoInternet))
        )
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("\$67,420").assertIsDisplayed()
    }

    @Test
    fun backButton_invokesCallback() {
        var backed = false
        setDetailScreen(onAction = { if (it == DetailAction.Back) backed = true })
        composeRule.onNodeWithContentDescription("Back").performClick()
        assertEquals(true, backed)
    }

    @Test
    fun candleStickChart_toggleFullScreen() {
        composeRule.setContent {
            var isFullScreen by remember { mutableStateOf(false) }
            SharedTransitionWrapper {
                DetailScreen(
                    state = DetailScreenState(
                        coinId = "bitcoin",
                        isChartFullScreen = isFullScreen,
                        content = successContent
                    ),
                    initialAsset = null,
                    onAction = { if (it == DetailAction.ToggleFullScreen) isFullScreen = !isFullScreen },
                    orientationController = testOrientationController
                )
            }
        }

        composeRule.onNodeWithContentDescription("Close fullscreen chart").assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription("Open fullscreen chart").performClick()

        composeRule.onNodeWithContentDescription("Open fullscreen chart").assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription("Close fullscreen chart").performClick()

        composeRule.onNodeWithContentDescription("Open fullscreen chart").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close fullscreen chart").assertIsNotDisplayed()
    }

    // ----- tabs -----

    @Test
    fun successState_showsStatsTabByDefault() {
        setDetailScreen()
        composeRule.onNodeWithText("Stats").assertIsDisplayed()
        composeRule.onNodeWithText("Alerts").assertIsDisplayed()
        composeRule.onNodeWithText("Market Cap").assertIsDisplayed()
    }

    @Test
    fun alertsTab_showsEmptyAlertsState() {
        setDetailScreen(state = defaultState(selectedTab = DetailTab.ALERTS))
        composeRule.onNodeWithText("No alerts yet").assertIsDisplayed()
    }

    @Test
    fun alertsTab_showsAddAlertButton() {
        setDetailScreen(state = defaultState(selectedTab = DetailTab.ALERTS))
        composeRule.onNodeWithText("Add Alert").assertIsDisplayed()
    }

    @Test
    fun alertsTab_tapAddAlert_invokesOnEditAlert() {
        var tapped = false
        setDetailScreen(
            state = defaultState(selectedTab = DetailTab.ALERTS),
            onAction = { if (it is DetailAction.EditAlert) tapped = true }
        )
        composeRule.onNodeWithText("Add Alert").performClick()
        assertEquals(true, tapped)
    }

    // ----- helpers -----

    private fun assertEquals(
        expected: Any?,
        actual: Any?
    ) {
        assert(expected == actual) { "Expected $expected but was $actual" }
    }
}
