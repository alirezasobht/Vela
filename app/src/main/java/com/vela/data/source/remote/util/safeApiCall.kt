package com.vela.data.source.remote.util

import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> {
    return try {
        DataResult.Success(block())
    } catch (e: IOException) {
        DataResult.Error(AppError.NoInternet)
    } catch (e: Exception) {
        DataResult.Error(AppError.Unknown(e.message ?: "Something went wrong"))
    }
}