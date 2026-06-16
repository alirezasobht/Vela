package com.vela.ui.screens.watchlist

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.theme.VelaTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun defaultActions(
        isWatchlisted: Boolean = true,
        onToggleWatchlist: (String) -> Unit = {}
    ) = WatchlistActions(
        assetListItemActions =
            AssetListItemActions(
                observePrice = { flowOf(null) },
                observeIsWatchlisted = { flowOf(isWatchlisted) },
                onToggleWatchlist = onToggleWatchlist,
                onClick = {}
            )
    )

    private val successState =
        WatchlistUiState.Success(
            assets = FakeAssetDataSource.assets.take(3).map { it.toUiModel() }
        )

    @Test
    fun loadingState_showsNoAssets() {
        composeRule.setContent {
            VelaTheme {
                WatchlistScreen(
                    uiState = WatchlistUiState.Loading,
                    watchlistActions = defaultActions()
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
    }

    @Test
    fun emptyState_showsEmptyTitleAndSubtitle() {
        composeRule.setContent {
            VelaTheme {
                WatchlistScreen(
                    uiState = WatchlistUiState.Empty,
                    watchlistActions = defaultActions()
                )
            }
        }
        composeRule.onNodeWithText("No coins yet").assertIsDisplayed()
        composeRule.onNodeWithText("Tap the star on any coin to add it here").assertIsDisplayed()
    }

    @Test
    fun successState_showsAssetNames() {
        composeRule.setContent {
            SharedTransitionWrapper {
                WatchlistScreen(
                    uiState = successState,
                    watchlistActions = defaultActions()
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("Ethereum").assertIsDisplayed()
        composeRule.onNodeWithText("Tether").assertIsDisplayed()
    }

    @Test
    fun successState_starButtonsVisibleForEachItem() {
        composeRule.setContent {
            SharedTransitionWrapper {
                WatchlistScreen(
                    uiState = successState,
                    watchlistActions = defaultActions(isWatchlisted = true)
                )
            }
        }
        composeRule.onAllNodesWithContentDescription("Watchlist").assertCountEquals(3)
    }

    @Test
    fun successState_tappingStar_invokesOnToggleWatchlist() {
        var toggledId: String? = null
        composeRule.setContent {
            SharedTransitionWrapper {
                WatchlistScreen(
                    uiState = successState,
                    watchlistActions =
                        defaultActions(
                            isWatchlisted = true,
                            onToggleWatchlist = { toggledId = it }
                        )
                )
            }
        }
        composeRule.onAllNodesWithContentDescription("Watchlist")[0].performClick()
        assert(toggledId == "bitcoin")
    }

    @Test
    fun successState_nonBlockingErrorBannerVisible() {
        composeRule.setContent {
            SharedTransitionWrapper {
                WatchlistScreen(
                    uiState = successState.copy(nonBlockingError = AppError.NoInternet),
                    watchlistActions = defaultActions()
                )
            }
        }
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }
}
