package com.vela.domain.repository

import androidx.paging.PagingData
import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import kotlinx.coroutines.flow.Flow

interface MarketsRepository {
    suspend fun getCategories(limit: Int = 20): DataResult<List<MarketCategory>>
    fun getMarkets(category: MarketCategory, sort: MarketSort): Flow<PagingData<Asset>>
    suspend fun getPrices(ids: List<String>): DataResult<List<Asset>>

}