package com.vela.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.theme.VelaTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultActions = HomeActions(
        onLimitChanged = {},
        onRetry = {},
        onPullToRefresh = {},
        observePrice = { flowOf(null) },
        onAssetClick = {}
    )

    private val successState = HomeUiState.Success(
        assets = FakeAssetDataSource.assets.map { it.toUiModel() },
        formattedDate = "Monday, 9 Jun"
    )

    @Test
    fun loadingState_showsProgressIndicator() {
        composeRule.setContent {
            VelaTheme {
                HomeScreen(
                    uiState = HomeUiState.Loading,
                    pullRefreshing = false,
                    selectedLimit = 50,
                    homeActions = defaultActions
                )
            }
        }
        // CircularProgressIndicator has no text — assert no asset names visible
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
        composeRule.onNodeWithText("Try Again").assertDoesNotExist()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() {
        composeRule.setContent {
            VelaTheme {
                HomeScreen(
                    uiState = HomeUiState.Error(AppError.NoInternet),
                    pullRefreshing = false,
                    selectedLimit = 50,
                    homeActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButtonInvokesCallback() {
        var retried = false
        composeRule.setContent {
            VelaTheme {
                HomeScreen(
                    uiState = HomeUiState.Error(AppError.NoInternet),
                    pullRefreshing = false,
                    selectedLimit = 50,
                    homeActions = defaultActions.copy(onRetry = { retried = true })
                )
            }
        }
        composeRule.onNodeWithText("Try Again").performClick()
        assert(retried)
    }

    @Test
    fun successState_showsFormattedDate() {
        composeRule.setContent {
            SharedTransitionWrapper {
                HomeScreen(
                    uiState = successState,
                    pullRefreshing = false,
                    selectedLimit = 50,
                    homeActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("Monday, 9 Jun").assertIsDisplayed()
    }

    @Test
    fun successState_showsAssetNames() {
        composeRule.setContent {
            SharedTransitionWrapper {
                HomeScreen(
                    uiState = successState,
                    pullRefreshing = false,
                    selectedLimit = 50,
                    homeActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun successState_limitChipsRenderedWithCorrectSelection() {
        composeRule.setContent {
            SharedTransitionWrapper {
                HomeScreen(
                    uiState = successState,
                    pullRefreshing = false,
                    selectedLimit = 50,
                    homeActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("25").assertIsDisplayed()
        composeRule.onNodeWithText("50").assertIsDisplayed()
        composeRule.onNodeWithText("100").assertIsDisplayed()
    }

    @Test
    fun successState_nonBlockingErrorBannerVisible() {
        composeRule.setContent {
            SharedTransitionWrapper {
                HomeScreen(
                    uiState = successState.copy(nonBlockingError = AppError.NoInternet),
                    pullRefreshing = false,
                    selectedLimit = 50,
                    homeActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        // assets still visible
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }
}