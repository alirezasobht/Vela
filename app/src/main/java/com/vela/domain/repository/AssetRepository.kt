package com.vela.domain.repository

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getTopAssets(limit: Int = 50): Flow<DataResult<List<Asset>>>
    suspend fun refresh(limit: Int = 50): DataResult<Unit>
    suspend fun getAssetsByIds(ids: List<String>): DataResult<List<Asset>>
}