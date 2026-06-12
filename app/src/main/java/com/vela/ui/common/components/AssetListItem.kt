package com.vela.ui.common.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vela.R
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.common.components.model.AssetListItemActions
import com.vela.ui.common.components.model.AssetUiModel
import com.vela.ui.common.util.LocalAnimatedVisibilityScope
import com.vela.ui.common.util.LocalSharedTransitionScope
import com.vela.ui.common.util.SharedTransitionWrapper
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AssetListItem(
    asset: AssetUiModel,
    actions: AssetListItemActions,
    showWatchlistButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { actions.onClick(asset.id) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        NameAndIconCell(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            asset = asset
        )

        Sparkline(
            prices = asset.sparkline,
            color = asset.color,
            modifier = Modifier.size(width = 60.dp, height = 30.dp)
        )

        PriceCell(
            id = asset.id,
            initialPrice = asset.price,
            initialPriceChange = asset.priceChange,
            initialColor = asset.color,
            observePrice = actions.observePrice
        )

        WatchlistButton(
            asset = asset,
            showWatchlistButton = showWatchlistButton,
            actions = actions
        )
    }
}

@Composable
private fun WatchlistButton(
    asset: AssetUiModel,
    showWatchlistButton: Boolean,
    actions: AssetListItemActions
) {
    if (showWatchlistButton) {
        val isWatchlisted by remember(asset.id) { actions.observeIsWatchlisted(asset.id) }
            .collectAsStateWithLifecycle(initialValue = false)

        IconButton(onClick = { actions.onToggleWatchlist(asset.id) }) {
            Icon(
                imageVector = if (isWatchlisted) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = stringResource(R.string.cd_watchlist),
                tint = if (isWatchlisted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RowScope.NameAndIconCell(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    asset: AssetUiModel
) {
    with(sharedTransitionScope) {
        AsyncImage(
            model = asset.image,
            contentDescription = asset.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                    if (animatedVisibilityScope != null) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "coin-image-${asset.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    } else Modifier
                ),
            placeholder = rememberVectorPainter(Icons.Default.Paid),
            error = rememberVectorPainter(Icons.Default.MonetizationOn)
        )
    }

    Column(modifier = Modifier.weight(1f)) {
        with(sharedTransitionScope) {
            Text(
                text = asset.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = if (animatedVisibilityScope != null) {
                    Modifier.sharedElement(
                        rememberSharedContentState(key = "coin-name-${asset.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                } else Modifier
            )
        }
        Text(
            text = asset.symbolAndRank,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----- Previews ------

@Preview(showBackground = true)
@Composable
private fun AssetListItemPreview() {
    SharedTransitionWrapper {
        AssetListItem(
            asset = FakeAssetDataSource.assets.first().toUiModel(),
            actions = AssetListItemActions(
                observePrice = { flowOf(null) },
                observeIsWatchlisted = { flowOf(false) },
                onToggleWatchlist = {},
                onClick = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AssetListItemWatchlistedPreview() {
    SharedTransitionWrapper {
        AssetListItem(
            asset = FakeAssetDataSource.assets.first().toUiModel(),
            actions = AssetListItemActions(
                observePrice = { flowOf(null) },
                observeIsWatchlisted = { flowOf(true) },
                onToggleWatchlist = {},
                onClick = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AssetListItemNoWatchlistButtonPreview() {
    SharedTransitionWrapper {
        AssetListItem(
            asset = FakeAssetDataSource.assets.first().toUiModel(),
            showWatchlistButton = false,
            actions = AssetListItemActions(
                observePrice = { flowOf(null) },
                observeIsWatchlisted = { flowOf(false) },
                onToggleWatchlist = {},
                onClick = {}
            )
        )
    }
}