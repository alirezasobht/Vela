package com.vela.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toDomain
import com.vela.data.source.remote.paging.MarketsPagingSource
import com.vela.data.source.remote.util.safeApiCall
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.domain.repository.MarketsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MarketsRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi
) : MarketsRepository {

    override suspend fun getCategories(limit: Int): DataResult<List<MarketCategory>> =
        safeApiCall {
            val categories = api.getCategories().take(limit).map { it.toDomain() }
            listOf(MarketCategory.ALL) + categories
        }

    override fun getMarkets(category: MarketCategory, sort: MarketSort): Flow<PagingData<Asset>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MarketsPagingSource(api, category, sort) }
        ).flow

    override suspend fun getPrices(ids: List<String>): DataResult<List<Asset>> =
        safeApiCall {
            api.getSimplePrices(ids = ids.joinToString(",")).toDomain()
        }
}