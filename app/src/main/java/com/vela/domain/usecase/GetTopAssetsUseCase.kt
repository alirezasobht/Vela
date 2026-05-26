package com.vela.domain.usecase

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import javax.inject.Inject

class GetTopAssetsUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    suspend operator fun invoke(limit: Int = 50): DataResult<List<Asset>> =
        repository.getTopAssets(limit)
}
