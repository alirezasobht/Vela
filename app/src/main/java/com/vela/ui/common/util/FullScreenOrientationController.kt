package com.vela.ui.common.util

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal fun interface FullScreenOrientationController {
    fun lockLandscape(): () -> Unit
}

private class ActivityFullScreenOrientationController(
    private val activity: Activity?
) : FullScreenOrientationController {
    override fun lockLandscape(): () -> Unit {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        return {
            activity?.requestedOrientation =
                originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

@Composable
internal fun rememberFullScreenOrientationController(): FullScreenOrientationController {
    val context = LocalContext.current
    val activity = context as? Activity
    return remember(activity) { ActivityFullScreenOrientationController(activity) }
}