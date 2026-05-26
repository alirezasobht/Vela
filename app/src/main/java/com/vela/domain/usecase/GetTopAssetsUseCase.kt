package com.vela.domain.usecase

import com.vela.domain.model.Asset
import com.vela.domain.repository.AssetRepository

class GetTopAssetsUseCase(
    private val repository: AssetRepository
) {
    suspend operator fun invoke(limit: Int = 50): List<Asset> =
        repository.getTopAssets(limit)
}
