package com.vela.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.ui.screens.home.HomeScreen
import com.vela.ui.screens.home.HomeViewModel

@Composable
fun VelaApp() {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold {
        Surface(modifier = Modifier.fillMaxSize()) {
            HomeScreen(uiState = uiState, onRetry = homeViewModel::retry)
        }
    }
}