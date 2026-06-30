package com.vela.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class DeleteSwipeState {
    Closed,
    Open
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToConfirmDeleteItem(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    actionWidth: Dp = 128.dp,
    deleteBackgroundColor: Color = Color(0xFFD32F2F),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val actionWidthPx = with(density) { actionWidth.toPx() }
    var contentHeightPx by remember { mutableStateOf(0) }
    val contentHeight = with(density) { contentHeightPx.toDp() }
    var removing by remember { mutableStateOf(false) }

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val state = remember {
        AnchoredDraggableState(
            initialValue = DeleteSwipeState.Closed,
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 80.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    LaunchedEffect(actionWidthPx) {
        state.updateAnchors(
            DraggableAnchors {
                DeleteSwipeState.Closed at 0f
                DeleteSwipeState.Open at -actionWidthPx
            }
        )
    }

    LaunchedEffect(removing) {
        if (removing) {
            delay(220.milliseconds)
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = !removing,
        modifier = modifier,
        exit = shrinkVertically(animationSpec = tween(durationMillis = 220)) +
            fadeOut(animationSpec = tween(durationMillis = 180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .background(deleteBackgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(actionWidth)
                    .height(contentHeight)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { state.animateTo(DeleteSwipeState.Closed) } }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }

                    IconButton(onClick = { removing = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        IntOffset(
                            x = state.offset.takeUnless { it.isNaN() }?.roundToInt() ?: 0,
                            y = 0
                        )
                    }
                    .anchoredDraggable(state = state, orientation = Orientation.Horizontal)
                    .onSizeChanged { contentHeightPx = it.height }
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                content()
            }
        }
    }
}
