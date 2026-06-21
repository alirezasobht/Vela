package com.vela.domain.usecase

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetTopAssetsUseCase @Inject constructor(private val repository: AssetRepository) {
    operator fun invoke(limit: Int = 50): Flow<DataResult<List<Asset>>> = repository.getTopAssets(limit)
}
