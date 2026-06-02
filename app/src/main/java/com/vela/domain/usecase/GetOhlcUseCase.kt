package com.vela.domain.usecase

import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint
import com.vela.domain.repository.DetailRepository
import javax.inject.Inject

class GetOhlcUseCase @Inject constructor(
    private val repository: DetailRepository
) {
    suspend operator fun invoke(id: String, days: Int): DataResult<List<OhlcPoint>> =
        repository.getOhlc(id, days)
}
