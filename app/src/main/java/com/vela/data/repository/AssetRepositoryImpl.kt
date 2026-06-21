package com.vela.data.repository

import com.vela.data.source.local.dao.HomeAssetDao
import com.vela.data.source.local.mapper.toDomain
import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toDomain
import com.vela.data.source.remote.mapper.toEntity
import com.vela.data.source.remote.util.safeApiCall
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AssetRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val dao: HomeAssetDao
) : AssetRepository {
    override fun getTopAssets(limit: Int): Flow<DataResult<List<Asset>>> = dao.observeAll(limit).map { entities ->
        DataResult.Success(entities.map { it.toDomain() })
    }

    override suspend fun fetchTopAssets(limit: Int): DataResult<Unit> = safeApiCall {
        val entities = api.getMarkets(limit = limit).map { it.toEntity() }
        dao.refresh(entities)
    }

    override suspend fun getAssetsByIds(ids: List<String>): DataResult<List<Asset>> = safeApiCall {
        api.getMarketsByIds(ids = ids.joinToString(",")).map { it.toDomain() }
    }
}
