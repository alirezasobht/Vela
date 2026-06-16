package com.vela.data.repository.fake

import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.repository.PriceRepository
import javax.inject.Inject

class FakePriceRepository
    @Inject
    constructor() : PriceRepository {
        override suspend fun getPrices(
            ids: List<String>,
            includeMarketData: Boolean
        ): DataResult<Map<String, SimplePrice>> = DataResult.Success(emptyMap())
    }
