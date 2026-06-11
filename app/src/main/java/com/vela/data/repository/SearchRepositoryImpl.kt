package com.vela.data.repository

import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toDomain
import com.vela.data.source.remote.util.safeApiCall
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi
) : SearchRepository {

    override suspend fun search(query: String): DataResult<List<Asset>> = safeApiCall {
        api.search(query).coins.map { it.toDomain() }
    }
}