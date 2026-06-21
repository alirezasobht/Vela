package com.vela.domain.usecase

import com.vela.domain.repository.WatchlistRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class IsWatchlistedUseCase @Inject constructor(private val repository: WatchlistRepository) {
    operator fun invoke(coinId: String): Flow<Boolean> = repository.isWatchlisted(coinId)
}
