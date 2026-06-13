package com.vela.data.repository.fake

import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeAssetRepository @Inject constructor() : AssetRepository {
    override fun getTopAssets(limit: Int): Flow<DataResult<List<Asset>>> =
        flowOf(DataResult.Success(FakeAssetDataSource.assets))

    override suspend fun fetchTopAssets(limit: Int): DataResult<Unit> =
        DataResult.Success(Unit)

    override suspend fun getAssetsByIds(ids: List<String>): DataResult<List<Asset>> =
        DataResult.Success(FakeAssetDataSource.assets.filter { it.id in ids })
}