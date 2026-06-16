package com.vela.domain.repository

import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun observeWatchlist(): Flow<List<String>>

    fun isWatchlisted(coinId: String): Flow<Boolean>

    suspend fun toggleWatchlist(coinId: String)
}
