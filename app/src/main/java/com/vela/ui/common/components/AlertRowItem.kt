package com.vela.ui.common.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.R
import com.vela.ui.common.util.LocalAnimatedVisibilityScope
import com.vela.ui.common.util.LocalSharedTransitionScope
import com.vela.ui.common.util.SharedTransitionKeys
import com.vela.ui.common.util.SharedTransitionWrapper
import com.vela.ui.screens.alerts.AlertRowUiModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AlertRowItem(
    alert: AlertRowUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = alert.label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (alert.isTriggered) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (alert.isTriggered) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedBounds(
                                    rememberSharedContentState(key = SharedTransitionKeys.alertLabel(alert.id)),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    enter = fadeIn(tween(300)),
                                    exit = fadeOut(tween(300)),
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            )
            if (alert.isTriggered) {
                Text(
                    text = stringResource(R.string.label_alert_triggered),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertRowItemPreview() {
    SharedTransitionWrapper {
        AlertRowItem(
            alert = AlertRowUiModel(id = 1L, label = "Price above \$80,000", isTriggered = false),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertRowItemTriggeredPreview() {
    SharedTransitionWrapper {
        AlertRowItem(
            alert = AlertRowUiModel(id = 2L, label = "Price below \$60,000", isTriggered = true),
            onClick = {}
        )
    }
}
