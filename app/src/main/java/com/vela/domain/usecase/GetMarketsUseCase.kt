package com.vela.domain.usecase

import androidx.paging.PagingData
import com.vela.domain.model.Asset
import com.vela.domain.model.MarketCategory
import com.vela.domain.model.MarketSort
import com.vela.domain.repository.MarketsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMarketsUseCase
    @Inject
    constructor(
        private val repository: MarketsRepository
    ) {
        operator fun invoke(
            category: MarketCategory,
            sort: MarketSort
        ): Flow<PagingData<Asset>> = repository.getMarkets(category, sort)
    }
