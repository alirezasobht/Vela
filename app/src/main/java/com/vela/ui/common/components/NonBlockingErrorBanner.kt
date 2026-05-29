package com.vela.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.domain.model.AppError
import com.vela.ui.common.extensions.toMessage
import com.vela.ui.theme.VelaTheme


@Composable
fun NonBlockingErrorBanner(
    error: AppError?,
    modifier: Modifier = Modifier
) {
    var lastError by remember { mutableStateOf(error) }
    if (error != null) lastError = error

    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        lastError?.let { activeError ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeError.toMessage(),
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NonBlockingErrorBannerPreview() {
    VelaTheme {
        NonBlockingErrorBanner(AppError.NoInternet)
    }
}