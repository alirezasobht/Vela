package com.vela.data.repository

import com.vela.data.source.local.dao.WatchlistDao
import com.vela.data.source.local.model.WatchlistEntity
import com.vela.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WatchlistRepositoryImpl @Inject constructor(
    private val dao: WatchlistDao
) : WatchlistRepository {

    override fun observeWatchlist(): Flow<List<String>> =
        dao.observeAll().map { list -> list.map { it.coinId } }

    override fun isWatchlisted(coinId: String): Flow<Boolean> =
        dao.isWatchlisted(coinId)

    override suspend fun toggleWatchlist(coinId: String) {
        val isWatchlisted = dao.isWatchlisted(coinId).first()
        if (isWatchlisted) {
            dao.delete(coinId)
        } else {
            dao.insert(WatchlistEntity(coinId))
        }
    }
}