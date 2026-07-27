package com.vela.data.repository

import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toDomain
import com.vela.data.source.remote.util.safeApiCall
import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.repository.PriceRepository
import javax.inject.Inject

class PriceRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val priceStore: SimplePriceStore
) : PriceRepository {
    override suspend fun getPrices(
        ids: List<String>,
        includeMarketData: Boolean
    ): DataResult<Map<String, SimplePrice>> = safeApiCall {
        api
            .getSimplePrices(
                ids = ids.joinToString(","),
                includeMarketCap = includeMarketData,
                includeVolume = includeMarketData
            ).mapValues { it.value.toDomain() }
    }.also { result ->
        if (result is DataResult.Success) priceStore.upsert(result.data)
    }
}
