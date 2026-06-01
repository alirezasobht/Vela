package com.vela.ui.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun ScreenVisibilityObserver(onScreenVisible: (Boolean) -> Unit) {

    val lastState = remember { mutableStateOf<Boolean?>(null) }

    fun updateState(isVisible: Boolean) {
        if (lastState.value != isVisible) {
            lastState.value = isVisible
            onScreenVisible(isVisible)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { updateState(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { updateState(false) }

    DisposableEffect(Unit) {
        onDispose { updateState(false) }
    }
}
