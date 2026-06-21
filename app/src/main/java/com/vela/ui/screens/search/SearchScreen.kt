package com.vela.ui.screens.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.AssetLazyList
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.util.ScreenVisibilityObserver
import com.vela.ui.common.util.SharedTransitionWrapper
import kotlinx.coroutines.flow.flowOf

internal data class SearchActions(
    val onQueryChange: (String) -> Unit,
    val onClearQuery: () -> Unit,
    val onRetry: () -> Unit,
    val assetListItemActions: AssetListItemActions
)

@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {
    ScreenVisibilityObserver(viewModel::onScreenVisible)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val searchActions = SearchActions(
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::onClearQuery,
        onRetry = viewModel::retry,
        assetListItemActions = viewModel.assetListItemActions(onClick = navigateToDetail)
    )

    SearchScreen(
        uiState = uiState,
        query = viewModel.query,
        searchActions = searchActions,
        modifier = modifier
    )
}

@Composable
internal fun SearchScreen(
    uiState: SearchUiState,
    query: String,
    searchActions: SearchActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        SearchBar(
            query = query,
            onQueryChange = searchActions.onQueryChange,
            onClearQuery = searchActions.onClearQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when (uiState) {
            is SearchUiState.Empty -> EmptyState()
            is SearchUiState.Loading -> FullScreenLoader()
            is SearchUiState.Results -> ResultsState(uiState = uiState, searchActions = searchActions)
            is SearchUiState.NoResults -> NoResultsState(query = query)
            is SearchUiState.Error -> FullScreenError(appError = uiState.appError, onRetry = searchActions.onRetry)
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        placeholder = {
            Text(
                text = stringResource(R.string.hint_search),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onClearQuery() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_clear_search)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.label_search_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultsState(
    uiState: SearchUiState.Results,
    searchActions: SearchActions,
    modifier: Modifier = Modifier
) {
    // hide the keyboard if scroll
    val keyboardController = LocalSoftwareKeyboardController.current
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && (available.y < -5 || available.y > 5)) {
                    keyboardController?.hide()
                }
                return Offset.Zero
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        NonBlockingErrorBanner(error = uiState.nonBlockingError)
        AssetLazyList(
            assets = uiState.assets,
            actions = searchActions.assetListItemActions,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection) // Attach the spy here
        )
    }
}

@Composable
private fun NoResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.label_no_results, query),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---- Previews ----

@Composable
private fun SearchScreenPreview(
    uiState: SearchUiState,
    query: String = ""
) {
    SharedTransitionWrapper {
        SearchScreen(
            uiState = uiState,
            query = query,
            searchActions = SearchActions(
                onQueryChange = {},
                onClearQuery = {},
                onRetry = {},
                assetListItemActions = AssetListItemActions(
                    observePrice = { flowOf(null) },
                    observeIsWatchlisted = { flowOf(false) },
                    onToggleWatchlist = {},
                    onClick = {}
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenEmptyPreview() {
    SearchScreenPreview(
        uiState = SearchUiState.Empty
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenLoadingPreview() {
    SearchScreenPreview(
        uiState = SearchUiState.Loading
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenResultsPreview() {
    SearchScreenPreview(
        uiState = SearchUiState.Results(
            assets = FakeAssetDataSource.assets.map { it.toUiModel() }
        ),
        query = "bitcoin"
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenResultsWithErrorPreview() {
    SearchScreenPreview(
        uiState = SearchUiState.Results(
            assets = FakeAssetDataSource.assets.map { it.toUiModel() },
            nonBlockingError = AppError.NoInternet
        ),
        query = "bitcoin"
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenNoResultsPreview() {
    SearchScreenPreview(
        uiState = SearchUiState.NoResults,
        query = "xyz123"
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenErrorPreview() {
    SearchScreenPreview(
        uiState = SearchUiState.Error(AppError.NoInternet),
        query = "bitcoin"
    )
}
