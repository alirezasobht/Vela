package com.vela.ui.screens.detail.chart

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.OhlcPoint
import com.vela.domain.model.TimeRange
import com.vela.ui.common.util.LandscapePreview
import com.vela.ui.theme.VelaTheme

@Composable
internal fun FullScreenChartDialog(
    ohlcPoints: List<OhlcPoint>,
    isChartLoading: Boolean,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation =
                originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {

        OhlcChartSection(
            ohlcPoints = ohlcPoints,
            isChartLoading = isChartLoading,
            selectedRange = selectedRange,
            onRangeSelected = onRangeSelected,
            isFullScreen = true,
            onToggleFullScreen = onDismiss,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        )
    }
}

// ------ Previews --------

@LandscapePreview
@Composable
private fun FullScreenChartDialogPreview() {
    VelaTheme {
        FullScreenChartDialog(
            ohlcPoints = FakeOhlcDataSource.bitcoinOhlc,
            isChartLoading = false,
            selectedRange = TimeRange.ONE_DAY,
            onRangeSelected = {},
            onDismiss = {}
        )
    }
}