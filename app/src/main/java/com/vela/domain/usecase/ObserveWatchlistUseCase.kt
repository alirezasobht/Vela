package com.vela.domain.usecase

import com.vela.domain.repository.WatchlistRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWatchlistUseCase @Inject constructor(private val repository: WatchlistRepository) {
    operator fun invoke(): Flow<List<String>> = repository.observeWatchlist()
}
