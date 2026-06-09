package com.vela.ui.screens.detail

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.fake.FakeDetailDataSource
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.AppError
import com.vela.domain.model.TimeRange
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.screens.detail.state.DetailUiState
import com.vela.ui.screens.detail.state.toUiModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalSharedTransitionApi::class)
class DetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultActions = DetailActions(
        onBack = {},
        onRetry = {},
        onRangeSelected = {},
        onToggleFullScreen = {},
        observePrice = { flowOf(null) }
    )

    private val successState = DetailUiState.Success(
        detail = FakeDetailDataSource.detail.toUiModel(),
        isChartLoading = false,
        ohlcPoints = FakeOhlcDataSource.bitcoinOhlc
    )

    private fun setDetailScreen(
        uiState: DetailUiState,
        actions: DetailActions = defaultActions
    ) {
        composeRule.setContent {
            SharedTransitionWrapper {
                DetailScreen(
                    uiState = uiState,
                    initialAsset = null,
                    selectedRange = TimeRange.ONE_DAY,
                    isChartFullScreen = false,
                    detailActions = actions
                )
            }
        }
    }

    @Test
    fun loadingState_showsNoContent() {
        setDetailScreen(DetailUiState.Loading)
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
        composeRule.onNodeWithText("Try Again").assertDoesNotExist()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() {
        setDetailScreen(DetailUiState.Error(AppError.NoInternet))
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButtonInvokesCallback() {
        var retried = false
        setDetailScreen(
            uiState = DetailUiState.Error(AppError.NoInternet),
            actions = defaultActions.copy(onRetry = { retried = true })
        )
        composeRule.onNodeWithText("Try Again").performClick()
        assert(retried)
    }

    @Test
    fun successState_showsCoinPrice() {
        setDetailScreen(successState)
        composeRule.onNodeWithText("\$67,420").assertIsDisplayed()
    }

    @Test
    fun successState_showsTimeRangeChips() {
        setDetailScreen(successState)
        composeRule.onNodeWithText("1D").assertIsDisplayed()
        composeRule.onNodeWithText("7D").assertIsDisplayed()
        composeRule.onNodeWithText("1M").assertIsDisplayed()
        composeRule.onNodeWithText("3M").assertIsDisplayed()
        composeRule.onNodeWithText("1Y").assertIsDisplayed()
    }

    @Test
    fun successState_showsMarketCapRankBadge() {
        setDetailScreen(successState)
        // FakeDetailDataSource.detail has marketCapRank = 27
        composeRule.onNodeWithText("#27").assertIsDisplayed()
    }

    @Test
    fun successState_marketCapRankBadge_hiddenWhenNull() {
        val detailNoRank = FakeDetailDataSource.detail.copy(marketCapRank = null).toUiModel()
        setDetailScreen(DetailUiState.Success(detail = detailNoRank, isChartLoading = false))
        composeRule.onNodeWithText("#", substring = true).assertDoesNotExist()
    }

    @Test
    fun successState_nonBlockingErrorBannerVisible() {
        setDetailScreen(successState.copy(nonBlockingError = AppError.NoInternet))
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("\$67,420").assertIsDisplayed()
    }

    @Test
    fun backButton_invokesCallback() {
        var backed = false
        setDetailScreen(
            uiState = successState,
            actions = defaultActions.copy(onBack = { backed = true })
        )
        composeRule.onNodeWithContentDescription("Back").performClick()
        assert(backed)
    }
}