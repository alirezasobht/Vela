package com.vela.domain.repository

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult

interface SearchRepository {
    suspend fun search(query: String): DataResult<List<Asset>>
}
