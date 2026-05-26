package com.vela.domain.repository

import com.vela.domain.model.Asset

interface AssetRepository {
    suspend fun getTopAssets(limit: Int = 50): List<Asset>
}