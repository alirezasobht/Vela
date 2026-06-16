package com.vela.domain.repository

import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice

interface PriceRepository {
    suspend fun getPrices(
        ids: List<String>,
        includeMarketData: Boolean
    ): DataResult<Map<String, SimplePrice>>
}
