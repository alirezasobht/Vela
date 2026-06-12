package com.vela.ui.base.watchlist

import kotlinx.coroutines.flow.Flow

interface WatchlistController {
    fun observeIsWatchlisted(coinId: String): Flow<Boolean>
    fun toggleWatchlist(coinId: String)
}