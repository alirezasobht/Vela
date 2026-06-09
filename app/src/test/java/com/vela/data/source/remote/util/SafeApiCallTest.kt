package com.vela.data.source.remote.util

import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SafeApiCallTest {

    // ----- safeApiCall -----

    @Test
    fun `successful API call returns Success`() = runTest {
        val result = safeApiCall { "success" }
        assertEquals(DataResult.Success("success"), result)
    }

    @Test
    fun `IOException maps to NoInternet`() = runTest {
        val result = safeApiCall { throw IOException() }
        assertEquals(DataResult.Error(AppError.NoInternet), result)
    }

    @Test
    fun `HttpException maps to ServerError`() = runTest {
        val result = safeApiCall {
            throw HttpException(Response.error<Any>(500, "".toResponseBody(null)))
        }
        assertEquals(DataResult.Error(AppError.ServerError), result)
    }

    @Test
    fun `Exception with message maps to Unknown with that message`() = runTest {
        val result = safeApiCall { throw Exception("something broke") }
        assertEquals(DataResult.Error(AppError.Unknown("something broke")), result)
    }

    @Test
    fun `Exception with null message maps to Unknown with fallback`() = runTest {
        val result = safeApiCall { throw Exception(null as String?) }
        assertEquals(DataResult.Error(AppError.Unknown("Something went wrong")), result)
    }

    // ------ Throwable.toAppError -------

    @Test
    fun `toAppError - IOException maps to NoInternet`() {
        assertEquals(AppError.NoInternet, IOException().toAppError())
    }

    @Test
    fun `toAppError - HttpException maps to ServerError`() {
        val error = HttpException(Response.error<Any>(500, "".toResponseBody(null))).toAppError()
        assertEquals(AppError.ServerError, error)
    }

    @Test
    fun `toAppError - unknown Throwable with null message maps to fallback`() {
        assertEquals(AppError.Unknown("Something went wrong"), RuntimeException(null as String?).toAppError())
    }
}