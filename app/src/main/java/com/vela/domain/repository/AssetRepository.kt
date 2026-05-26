package com.vela.domain.repository

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult

interface AssetRepository {
    suspend fun getTopAssets(limit: Int = 50): DataResult<List<Asset>>
}