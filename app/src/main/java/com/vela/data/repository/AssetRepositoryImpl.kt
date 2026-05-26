package com.vela.data.repository

import com.vela.data.source.remote.RetrofitClient
import com.vela.data.source.remote.mapper.toDomain
import com.vela.domain.model.Asset
import com.vela.domain.repository.AssetRepository

class AssetRepositoryImpl : AssetRepository {

    private val api = RetrofitClient.api

    override suspend fun getTopAssets(limit: Int): List<Asset> {
        return api.getMarkets(limit = limit).map { it.toDomain() }
    }
}