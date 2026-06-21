package com.vela.data.source.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.data.source.remote.mapper.toDomain
import com.vela.domain.model.Asset
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort

class MarketsPagingSource(private val api: CoinGeckoApi, private val category: MarketCategory, private val sort: MarketSort) : PagingSource<Int, Asset>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Asset> {
        val page = params.key ?: 1
        return try {
            val results = api
                .getMarkets(
                    limit = params.loadSize,
                    page = page,
                    category = category.id,
                    order = sort.toApiValue()
                ).map { it.toDomain() }

            LoadResult.Page(
                data = results,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (results.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Asset>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }

    private fun MarketSort.toApiValue(): String = when (this) {
        MarketSort.MARKET_CAP -> "market_cap_desc"
        MarketSort.VOLUME -> "volume_desc"
    }
}
