package com.vela.ui.common.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.vela.ui.theme.VelaTheme

/**
 * A wrapper that provides the necessary [SharedTransitionScope] and [AnimatedVisibilityScope]
 * for shared element transitions in previews and tests.
 */
@Composable
fun SharedTransitionWrapper(content: @Composable () -> Unit) {
    VelaTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this@AnimatedVisibility
                ) {
                    content()
                }
            }
        }
    }
}
