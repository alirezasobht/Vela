package com.vela.ui.screens.home

import androidx.lifecycle.ViewModel
import com.vela.data.source.local.FakeAssetDataSource
import com.vela.ui.common.components.mapper.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            HomeUiState(
                assets = FakeAssetDataSource.assets.map { it.toUiModel() },
                date = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
            )
        }
    }
}