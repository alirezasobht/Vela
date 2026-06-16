package com.vela.data.repository.fake

import com.vela.data.source.fake.FakeAssetDataSource
import com.vela.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FakeWatchlistRepository
    @Inject
    constructor() : WatchlistRepository {
        private val watchlist =
            MutableStateFlow(
                FakeAssetDataSource.assets.filterIndexed { index, _ -> index < 3 }.map { it.id }
            )

        override fun observeWatchlist(): Flow<List<String>> = watchlist.map { it.toList() }

        override fun isWatchlisted(coinId: String): Flow<Boolean> = watchlist.map { it.contains(coinId) }

        override suspend fun toggleWatchlist(coinId: String) {
            watchlist.update { current ->
                if (current.contains(coinId)) {
                    current - coinId
                } else {
                    current + coinId
                }
            }
        }
    }
