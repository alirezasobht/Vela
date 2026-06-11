package com.vela.domain.usecase

import com.vela.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWatchlistUseCase @Inject constructor(
    private val repository: WatchlistRepository
) {
    operator fun invoke(): Flow<List<String>> = repository.observeWatchlist()
}