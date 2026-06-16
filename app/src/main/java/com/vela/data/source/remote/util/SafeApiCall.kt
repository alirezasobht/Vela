package com.vela.data.source.remote.util

import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: IOException) {
        DataResult.Error(AppError.NoInternet)
    } catch (e: HttpException) {
        DataResult.Error(AppError.ServerError)
    } catch (e: Exception) {
        DataResult.Error(AppError.Unknown(e.message ?: "Something went wrong"))
    }

fun Throwable.toAppError(): AppError =
    when (this) {
        is IOException -> AppError.NoInternet
        is HttpException -> AppError.ServerError
        else -> AppError.Unknown(message ?: "Something went wrong")
    }
