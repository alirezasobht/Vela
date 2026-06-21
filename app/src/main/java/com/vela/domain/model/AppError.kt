package com.vela.domain.model

sealed class AppError {
    data object NoInternet : AppError()

    data object ServerError : AppError()

    data class Unknown(val message: String) : AppError()
}
