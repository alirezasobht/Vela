package com.vela.ui.screens.markets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.data.source.fake.FakeCategoryDataSource
import com.vela.domain.model.AppError
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.theme.VelaTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultActions = MarketActions(
        onCategorySelected = {},
        onSortSelected = {},
        onRetry = {},
        assetListItemActions = AssetListItemActions(
            observePrice = { flowOf(null) },
            observeIsWatchlisted = { flowOf(false) },
            onToggleWatchlist = {},
            onClick = {}
        )
    )

    private val fakePagingItems
        @androidx.compose.runtime.Composable
        get() = flowOf(
            PagingData.from(FakeAssetDataSource.assets.map { it.toUiModel() })
        ).collectAsLazyPagingItems()

    @Test
    fun loadingState_showsProgressIndicator() {
        composeRule.setContent {
            VelaTheme {
                MarketsScreen(
                    uiState = MarketsUiState.Loading,
                    categories = FakeCategoryDataSource.categories,
                    selectedCategory = MarketCategory.ALL,
                    selectedSort = MarketSort.MARKET_CAP,
                    sorts = MarketSort.entries,
                    pagingItems = fakePagingItems,
                    marketAction = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
        composeRule.onNodeWithText("Try Again").assertDoesNotExist()
    }

    @Test
    fun errorState_showsErrorAndRetryButton() {
        composeRule.setContent {
            VelaTheme {
                MarketsScreen(
                    uiState = MarketsUiState.Error(AppError.NoInternet),
                    categories = FakeCategoryDataSource.categories,
                    selectedCategory = MarketCategory.ALL,
                    selectedSort = MarketSort.MARKET_CAP,
                    sorts = MarketSort.entries,
                    pagingItems = fakePagingItems,
                    marketAction = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun filterChip_showsSelectedCategoryName() {
        composeRule.setContent {
            VelaTheme {
                MarketsScreen(
                    uiState = MarketsUiState.Success(),
                    categories = FakeCategoryDataSource.categories,
                    selectedCategory = MarketCategory.ALL,
                    selectedSort = MarketSort.MARKET_CAP,
                    sorts = MarketSort.entries,
                    pagingItems = fakePagingItems,
                    marketAction = defaultActions
                )
            }
        }
        composeRule.onNodeWithText(MarketCategory.ALL.displayName).assertIsDisplayed()
    }

    @Test
    fun sortChip_showsSelectedSortName() {
        composeRule.setContent {
            VelaTheme {
                MarketsScreen(
                    uiState = MarketsUiState.Success(),
                    categories = FakeCategoryDataSource.categories,
                    selectedCategory = MarketCategory.ALL,
                    selectedSort = MarketSort.VOLUME,
                    sorts = MarketSort.entries,
                    pagingItems = fakePagingItems,
                    marketAction = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("Volume").assertIsDisplayed()
    }

    @Test
    fun filterChip_click_showsCategoryBottomSheet() {
        composeRule.setContent {
            VelaTheme {
                MarketsScreen(
                    uiState = MarketsUiState.Success(),
                    categories = FakeCategoryDataSource.categories,
                    selectedCategory = MarketCategory.ALL,
                    selectedSort = MarketSort.MARKET_CAP,
                    sorts = MarketSort.entries,
                    pagingItems = fakePagingItems,
                    marketAction = defaultActions
                )
            }
        }
        composeRule.onNodeWithText(MarketCategory.ALL.displayName).performClick()
        composeRule.onNodeWithText("Category").assertIsDisplayed()
    }

    @Test
    fun sortChip_click_showsSortBottomSheet() {
        composeRule.setContent {
            VelaTheme {
                MarketsScreen(
                    uiState = MarketsUiState.Success(),
                    categories = FakeCategoryDataSource.categories,
                    selectedCategory = MarketCategory.ALL,
                    selectedSort = MarketSort.MARKET_CAP,
                    sorts = MarketSort.entries,
                    pagingItems = fakePagingItems,
                    marketAction = defaultActions
                )
            }
        }
        composeRule.onNodeWithText("Market Cap").performClick()
        composeRule.onNodeWithText("Sort by").assertIsDisplayed()
    }
}