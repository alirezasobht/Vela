package com.vela.ui.screens.detail.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.R
import com.vela.ui.theme.VelaTheme

@Composable
internal fun FullScreenToggle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .background(
                color = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(6.dp)
        ) {
            Icon(
                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = stringResource(if (isFullScreen) R.string.cd_fullscreen_chart_close else R.string.cd_fullscreen_chart_open),
                tint = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun FullScreenTogglePreview() {
    VelaTheme {
        Surface(color = Color.White) {
            FullScreenToggle(isFullScreen = false, onClick = {})
        }
    }
}

@Preview
@Composable
private fun FullScreenLandscapeTogglePreview() {
    VelaTheme {
        Surface(color = Color.White) {
            FullScreenToggle(isFullScreen = true, onClick = {})
        }
    }
}
