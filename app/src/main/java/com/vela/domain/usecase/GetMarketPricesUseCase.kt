package com.vela.domain.usecase

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.MarketsRepository
import javax.inject.Inject

class GetMarketPricesUseCase @Inject constructor(
    private val repository: MarketsRepository
) {
    suspend operator fun invoke(ids: List<String>): DataResult<List<Asset>> =
        repository.getPrices(ids)
}