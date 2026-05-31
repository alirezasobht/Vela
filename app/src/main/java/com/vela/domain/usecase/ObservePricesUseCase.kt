@file:OptIn(ExperimentalCoroutinesApi::class)

package com.vela.domain.usecase

import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.repository.PriceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObservePricesUseCase @Inject constructor(
    private val repository: PriceRepository
) {
    operator fun invoke(ids: Flow<List<String>>): Flow<DataResult<Map<String, SimplePrice>>> =
        ids.flatMapLatest { idList ->
            if (idList.isEmpty()) flowOf(DataResult.Success(emptyMap()))
            else flow {
                while (true) {
                    emit(repository.getPrices(idList))
                    delay(5_000L)
                }
            }
        }
}
