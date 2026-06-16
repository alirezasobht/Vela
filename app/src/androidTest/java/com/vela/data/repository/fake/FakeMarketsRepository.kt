package com.vela.data.repository.fake

import androidx.paging.PagingData
import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.data.source.fake.FakeCategoryDataSource
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.domain.repository.MarketsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeMarketsRepository
    @Inject
    constructor() : MarketsRepository {
        override suspend fun getCategories(limit: Int): DataResult<List<MarketCategory>> = DataResult.Success(FakeCategoryDataSource.categories)

        override fun getMarkets(
            category: MarketCategory,
            sort: MarketSort
        ): Flow<PagingData<Asset>> = flowOf(PagingData.from(FakeAssetDataSource.assets))
    }
