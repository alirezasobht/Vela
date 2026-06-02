package com.vela.domain.usecase

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.SearchRepository
import javax.inject.Inject

class SearchAssetsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: String): DataResult<List<Asset>> =
        when (val searchResult = repository.search(query)) {
            is DataResult.Error -> searchResult
            is DataResult.Success -> fetchPricesForAssets(searchResult.data)
        }

    private suspend fun fetchPricesForAssets(assets: List<Asset>): DataResult<List<Asset>> =
        if (assets.isEmpty()) {
            DataResult.Success(emptyList())
        } else {
            assets.map { it.id }.let { ids ->
                when (val pricesResult = repository.getPricesByIds(ids)) {
                    is DataResult.Success -> DataResult.Success(pricesResult.data)
                    is DataResult.Error -> DataResult.Success(assets)
                }
            }
        }
}

