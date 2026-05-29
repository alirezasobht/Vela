package com.vela.ui.screens.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.AppError
import com.vela.ui.common.components.AssetLazyList
import com.vela.ui.common.components.FullScreenError
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.NonBlockingErrorBanner
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.theme.VelaTheme

@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        uiState = uiState,
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::retry,
        modifier = modifier
    )
}

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when (uiState) {
            is SearchUiState.Empty -> EmptyState()
            is SearchUiState.Loading -> FullScreenLoader()
            is SearchUiState.Results -> ResultsState(uiState = uiState)
            is SearchUiState.NoResults -> NoResultsState(query = query)
            is SearchUiState.Error -> FullScreenError(appError = uiState.appError, onRetry = onRetry)
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
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
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_search)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
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
            text = stringResource(R.string.search_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultsState(
    uiState: SearchUiState.Results,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        NonBlockingErrorBanner(error = uiState.nonBlockingError)
        AssetLazyList(
            assets = uiState.assets,
            modifier = Modifier.fillMaxSize()
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
            text = stringResource(R.string.search_no_results, query),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun SearchScreenEmptyPreview() {
    VelaTheme {
        SearchScreen(
            uiState = SearchUiState.Empty,
            query = "",
            onQueryChange = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenLoadingPreview() {
    VelaTheme {
        SearchScreen(
            uiState = SearchUiState.Loading,
            query = "bitcoin",
            onQueryChange = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenResultsPreview() {
    VelaTheme {
        SearchScreen(
            uiState = SearchUiState.Results(
                assets = FakeAssetDataSource.assets.map { it.toUiModel() }
            ),
            query = "bitcoin",
            onQueryChange = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenResultsWithErrorPreview() {
    VelaTheme {
        SearchScreen(
            uiState = SearchUiState.Results(
                assets = FakeAssetDataSource.assets.map { it.toUiModel() },
                nonBlockingError = AppError.NoInternet
            ),
            query = "bitcoin",
            onQueryChange = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenNoResultsPreview() {
    VelaTheme {
        SearchScreen(
            uiState = SearchUiState.NoResults,
            query = "xyz123",
            onQueryChange = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenErrorPreview() {
    VelaTheme {
        SearchScreen(
            uiState = SearchUiState.Error(AppError.NoInternet),
            query = "bitcoin",
            onQueryChange = {},
            onRetry = {}
        )
    }
}