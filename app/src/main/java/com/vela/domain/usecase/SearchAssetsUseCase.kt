package com.vela.domain.usecase

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.SearchRepository
import javax.inject.Inject

class SearchAssetsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: String): DataResult<List<Asset>> =
        repository.search(query)
}