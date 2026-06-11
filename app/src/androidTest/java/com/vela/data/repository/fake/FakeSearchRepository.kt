package com.vela.data.repository.fake

import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.SearchRepository
import javax.inject.Inject

class FakeSearchRepository @Inject constructor() : SearchRepository {
    override suspend fun search(query: String): DataResult<List<Asset>> =
        DataResult.Success(FakeAssetDataSource.assets)

    override suspend fun getPricesByIds(ids: List<String>): DataResult<List<Asset>> =
        DataResult.Success(FakeAssetDataSource.assets)
}