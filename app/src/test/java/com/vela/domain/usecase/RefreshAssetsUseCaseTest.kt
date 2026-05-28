package com.vela.domain.usecase

import com.vela.domain.model.AppError
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshAssetsUseCaseTest {

    private val repository: AssetRepository = mockk()
    private val useCase = RefreshAssetsUseCase(repository)

    @Test
    fun `invoke returns success on repository success`() = runTest {
        coEvery { repository.refresh(any()) } returns DataResult.Success(Unit)

        val result = useCase()

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `invoke returns error on repository failure`() = runTest {
        coEvery { repository.refresh(any()) } returns DataResult.Error(AppError.NoInternet)

        val result = useCase()

        assertTrue(result is DataResult.Error)
        assertEquals(AppError.NoInternet, (result as DataResult.Error).appError)
    }

    @Test
    fun `invoke passes correct limit to repository`() = runTest {
        coEvery { repository.refresh(25) } returns DataResult.Success(Unit)

        useCase(25)

        coVerify { repository.refresh(25) }
    }

    @Test
    fun `invoke uses default limit value`() = runTest {
        coEvery { repository.refresh(50) } returns DataResult.Success(Unit)

        useCase()

        coVerify { repository.refresh(50) }
    }

    @Test
    fun `invoke handles zero as limit`() = runTest {
        coEvery { repository.refresh(0) } returns DataResult.Success(Unit)

        useCase(0)

        coVerify { repository.refresh(0) }
    }

    @Test
    fun `invoke handles negative limit`() = runTest {
        coEvery { repository.refresh(-1) } returns DataResult.Success(Unit)

        useCase(-1)

        coVerify { repository.refresh(-1) }
    }

    @Test
    fun `invoke handles maximum integer limit`() = runTest {
        coEvery { repository.refresh(Int.MAX_VALUE) } returns DataResult.Success(Unit)

        useCase(Int.MAX_VALUE)

        coVerify { repository.refresh(Int.MAX_VALUE) }
    }

    @Test
    fun `invoke repository exception bubbling`() = runTest {
        coEvery { repository.refresh(any()) } throws RuntimeException("Unexpected")

        var exception: RuntimeException? = null
        try {
            useCase()
        } catch (e: RuntimeException) {
            exception = e
        }

        assertEquals("Unexpected", exception?.message)
    }

    @Test
    fun `invoke execution on correct dispatcher`() = runTest {
        coEvery { repository.refresh(any()) } returns DataResult.Success(Unit)

        val result = useCase()

        assertTrue(result is DataResult.Success)
        coVerify(exactly = 1) { repository.refresh(any()) }
    }
}