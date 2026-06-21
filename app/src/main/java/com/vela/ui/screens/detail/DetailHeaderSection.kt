package com.vela.ui.screens.detail

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vela.R
import com.vela.ui.common.util.LocalAnimatedVisibilityScope
import com.vela.ui.common.util.LocalSharedTransitionScope
import com.vela.ui.common.util.SharedTransitionWrapper

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HeaderSection(
    name: String,
    symbol: String,
    image: String?,
    marketCapRank: Int?,
    coinId: String,
    isWatchlisted: Boolean,
    onBack: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back)
            )
        }

        with(sharedTransitionScope) {
            AsyncImage(
                model = image,
                contentDescription = name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (animatedVisibilityScope != null) {
                            Modifier.sharedElement(
                                rememberSharedContentState(key = "coin-image-$coinId"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        } else {
                            Modifier
                        }
                    ),
                placeholder = rememberVectorPainter(Icons.Default.Paid),
                error = rememberVectorPainter(Icons.Default.MonetizationOn)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            with(sharedTransitionScope) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = if (animatedVisibilityScope != null) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "coin-name-$coinId"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    } else {
                        Modifier
                    }
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                marketCapRank?.let {
                    Text(
                        text = "#$it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ).padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        IconButton(onClick = onToggleWatchlist) {
            Icon(
                imageVector = if (isWatchlisted) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = stringResource(R.string.cd_watchlist),
                tint = if (isWatchlisted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// ---- Previews ----

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun HeaderSectionPreview() {
    SharedTransitionWrapper {
        HeaderSection(
            name = "Bitcoin",
            symbol = "BTC",
            image = null,
            marketCapRank = 1,
            coinId = "bitcoin",
            isWatchlisted = false,
            onBack = {},
            onToggleWatchlist = {}
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun HeaderSectionWatchlistedPreview() {
    SharedTransitionWrapper {
        HeaderSection(
            name = "Bitcoin",
            symbol = "BTC",
            image = null,
            marketCapRank = 1,
            coinId = "bitcoin",
            isWatchlisted = true,
            onBack = {},
            onToggleWatchlist = {}
        )
    }
}
