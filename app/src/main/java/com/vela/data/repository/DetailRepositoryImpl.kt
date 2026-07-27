package com.vela.data.repository

import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toDomain
import com.vela.data.source.remote.mapper.toOhlcPoint
import com.vela.data.source.remote.mapper.toSimplePrice
import com.vela.data.source.remote.util.safeApiCall
import com.vela.domain.model.CoinDetail
import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint
import com.vela.domain.pricestore.SimplePriceStore
import com.vela.domain.repository.DetailRepository
import javax.inject.Inject

class DetailRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val priceStore: SimplePriceStore
) : DetailRepository {
    override suspend fun getCoinDetail(id: String): DataResult<CoinDetail> = safeApiCall {
        val dto = api.getCoinDetail(id = id).first()
        priceStore.upsert(mapOf(dto.id to dto.toSimplePrice()))
        dto.toDomain()
    }

    override suspend fun getOhlc(
        id: String,
        days: Int
    ): DataResult<List<OhlcPoint>> = safeApiCall {
        api.getOhlc(id = id, days = days).map { it.toOhlcPoint() }
    }
}
