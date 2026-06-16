package com.vela.ui.screens.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.util.SharedTransitionWrapper
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val defaultActions =
        SearchActions(
            onQueryChange = {},
            onClearQuery = {},
            onRetry = {},
            assetListItemActions =
                AssetListItemActions(
                    observePrice = { flowOf(null) },
                    observeIsWatchlisted = { flowOf(false) },
                    onToggleWatchlist = {},
                    onClick = {}
                )
        )

    @Test
    fun emptyState_showsHintText() {
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState = SearchUiState.Empty,
                    query = "",
                    searchActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("Start typing to search").assertIsDisplayed()
    }

    @Test
    fun noResultsState_showsQueryInMessage() {
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState = SearchUiState.NoResults,
                    query = "xyz123",
                    searchActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("No results for xyz123").assertIsDisplayed()
    }

    @Test
    fun resultsState_showsAssetList() {
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState =
                        SearchUiState.Results(
                            assets = FakeAssetDataSource.assets.map { it.toUiModel() }
                        ),
                    query = "bitcoin",
                    searchActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun clearButton_notVisibleWhenQueryEmpty() {
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState = SearchUiState.Empty,
                    query = "",
                    searchActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
    }

    @Test
    fun clearButton_visibleWhenQueryNonEmpty() {
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState = SearchUiState.Loading,
                    query = "bitcoin",
                    searchActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }

    @Test
    fun clearButton_invokesCallback() {
        var cleared = false
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState = SearchUiState.Loading,
                    query = "bitcoin",
                    searchActions = defaultActions.copy(onClearQuery = { cleared = true })
                )
            }
        }
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        assert(cleared)
    }

    @Test
    fun errorState_showsRetryButton() {
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState = SearchUiState.Error(AppError.NoInternet),
                    query = "",
                    searchActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun resultsState_nonBlockingErrorBannerVisible() {
        composeRule.setContent {
            SharedTransitionWrapper {
                SearchScreen(
                    uiState =
                        SearchUiState.Results(
                            assets = FakeAssetDataSource.assets.map { it.toUiModel() },
                            nonBlockingError = AppError.NoInternet
                        ),
                    query = "bitcoin",
                    searchActions = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }
}
