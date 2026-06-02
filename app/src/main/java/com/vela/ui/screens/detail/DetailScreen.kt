package com.vela.ui.screens.detail

import androidx.compose.runtime.Composable
import com.vela.ui.common.components.PlaceholderScreen

@Composable
fun DetailRoute(coinId: String, onBack: () -> Unit) {
    PlaceholderScreen(title = coinId)
}