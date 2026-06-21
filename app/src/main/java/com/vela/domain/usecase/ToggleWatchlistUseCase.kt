package com.vela.domain.usecase

import com.vela.domain.repository.WatchlistRepository
import javax.inject.Inject

class ToggleWatchlistUseCase @Inject constructor(private val repository: WatchlistRepository) {
    suspend operator fun invoke(coinId: String) = repository.toggleWatchlist(coinId)
}
