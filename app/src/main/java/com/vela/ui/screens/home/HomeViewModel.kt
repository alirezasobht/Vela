package com.vela.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetTodayUseCase
import com.vela.domain.usecase.GetTopAssetsUseCase
import com.vela.ui.common.components.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopAssets: GetTopAssetsUseCase,
    private val getToday: GetTodayUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAssets()
    }

    fun retry() {
        loadAssets()
    }

    private fun loadAssets() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            when (val result = getTopAssets()) {
                is DataResult.Success -> _uiState.value = HomeUiState.Success(
                    assets = result.data.map { it.toUiModel() },
                    date = formatDate(getToday())
                )
                is DataResult.Error -> _uiState.value = HomeUiState.Error(
                    message = when (result.exception) {
                        is AppError.NoInternet -> "No internet connection"
                        is AppError.ServerError -> "Server error, please try again"
                        is AppError.Unknown -> result.exception.message
                    }
                )
            }
        }
    }

    private fun formatDate(date: LocalDate) = date.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
}