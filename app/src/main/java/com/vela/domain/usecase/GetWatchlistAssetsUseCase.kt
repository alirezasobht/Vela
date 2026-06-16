package com.vela.domain.usecase

import com.vela.domain.model.Asset
import com.vela.domain.model.DataResult
import com.vela.domain.repository.AssetRepository
import com.vela.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

class GetWatchlistAssetsUseCase
    @Inject
    constructor(
        private val watchlistRepository: WatchlistRepository,
        private val assetRepository: AssetRepository
    ) {
        operator fun invoke(): Flow<DataResult<List<Asset>>> =
            watchlistRepository
                .observeWatchlist()
                .transformLatest { ids ->
                    if (ids.isEmpty()) {
                        emit(DataResult.Success(emptyList()))
                    } else {
                        emit(assetRepository.getAssetsByIds(ids))
                    }
                }
    }
