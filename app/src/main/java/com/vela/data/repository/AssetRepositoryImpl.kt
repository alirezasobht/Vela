package com.vela.data.repository

import com.vela.data.source.local.dao.HomeAssetDao
import com.vela.data.source.local.mapper.toDomain
import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toEntity
import com.vela.domain.model.AppError
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val dao: HomeAssetDao
) : AssetRepository {

    override fun getTopAssets(limit: Int): Flow<DataResult<List<Asset>>> =
        dao.observeAll(limit).map { entities ->
            DataResult.Success(entities.map { it.toDomain() })
        }

    override suspend fun refresh(limit: Int): DataResult<Unit> {
        return try {
            val entities = api.getMarkets(limit = limit).map { it.toEntity() }
            dao.refresh(entities)
            DataResult.Success(Unit)
        } catch (e: IOException) {
            DataResult.Error(AppError.NoInternet)
        } catch (e: Exception) {
            DataResult.Error(AppError.Unknown(e.message ?: "Something went wrong"))
        }
    }
}