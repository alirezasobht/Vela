package com.vela.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vela.data.repository.AssetRepositoryImpl
import com.vela.data.source.remote.RetrofitClient
import com.vela.domain.model.AppException
import com.vela.domain.model.DataResult
import com.vela.domain.usecase.GetTopAssetsUseCase
import com.vela.ui.common.components.mapper.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel : ViewModel() {

    private val repository = AssetRepositoryImpl(RetrofitClient.api)
    private val getTopAssets = GetTopAssetsUseCase(repository)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val date = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, d MMM"))

    init {
        loadAssets()
    }

    fun retry() {
        loadAssets()
    }

    private fun loadAssets() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result = getTopAssets()) {
                is DataResult.Success -> _uiState.value = HomeUiState.Success(
                    assets = result.data.map { it.toUiModel() },
                    date = date
                )

                is DataResult.Error -> _uiState.value = HomeUiState.Error(
                    message = when (result.exception) {
                        is AppException.NoInternet -> "No internet connection"
                        is AppException.ServerError -> "Server error, please try again"
                        is AppException.Unknown -> result.exception.message
                    } ?: "Something went wrong"
                )
            }
        }
    }
}