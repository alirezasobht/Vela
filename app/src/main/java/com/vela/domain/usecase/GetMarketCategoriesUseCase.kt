package com.vela.domain.usecase

import com.vela.domain.model.DataResult
import com.vela.domain.model.MarketCategory
import com.vela.domain.repository.MarketsRepository
import javax.inject.Inject

class GetMarketCategoriesUseCase @Inject constructor(
    private val repository: MarketsRepository
) {
    suspend operator fun invoke(limit: Int = 20): DataResult<List<MarketCategory>> =
        repository.getCategories(limit)
}