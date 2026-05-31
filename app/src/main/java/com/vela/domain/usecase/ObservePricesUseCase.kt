package com.vela.domain.usecase

import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.repository.PriceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObservePricesUseCase @Inject constructor(
    private val repository: PriceRepository,
    private val pollingInterval: Long = 30_000L
) {
    operator fun invoke(ids: Flow<List<String>>): Flow<Map<String, SimplePrice?>> =
        ids.flatMapLatest { idList ->
            if (idList.isEmpty()) flowOf(emptyMap())
            else flow {
                while (true) {
                    when (val result = repository.getPrices(idList)) {
                        is DataResult.Success -> emit(result.data)
                        is DataResult.Error -> { /* keep last value, don't emit on error */ }
                    }
                    delay(pollingInterval)
                }
            }
        }
}
