package com.vela.domain.usecase

import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.repository.PriceRepository
import javax.inject.Inject

class GetPricesOnlyUseCase @Inject constructor(private val repository: PriceRepository) : GetPricesUseCase {
    override suspend operator fun invoke(ids: List<String>): DataResult<Map<String, SimplePrice>> = repository.getPrices(ids, includeMarketData = false)
}
