package com.vela.domain.model

sealed class AppException : Exception() {
    data object NoInternet : AppException()
    data object ServerError : AppException()
    data class Unknown(val msg: String) : AppException()
}