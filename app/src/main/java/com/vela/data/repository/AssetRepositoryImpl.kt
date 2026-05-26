package com.vela.data.repository

import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toDomain
import com.vela.domain.model.AppException
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import java.io.IOException

class AssetRepositoryImpl(
    private val api: CoinGeckoApi
) : AssetRepository {

    override suspend fun getTopAssets(limit: Int): DataResult<List<Asset>> {
        return try {
            val assets = api.getMarkets(limit = limit).map { it.toDomain() }
            DataResult.Success(assets)
        } catch (e: IOException) {
            DataResult.Error(AppException.NoInternet)
        } catch (e: Exception) {
            DataResult.Error(AppException.Unknown(e.message ?: "Something went wrong"))
        }
    }
}