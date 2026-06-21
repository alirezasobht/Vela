package com.vela.ui.base.watchlist

import com.vela.domain.usecase.IsWatchlistedUseCase
import com.vela.domain.usecase.ToggleWatchlistUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WatchlistControllerImpl @Inject constructor(
    private val isWatchlistedUseCase: IsWatchlistedUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : WatchlistController {
    private var scope: CoroutineScope? = null

    fun bind(scope: CoroutineScope) {
        this.scope = scope
    }

    override fun observeIsWatchlisted(coinId: String): Flow<Boolean> = isWatchlistedUseCase(coinId)

    override fun toggleWatchlist(coinId: String) {
        scope?.launch { toggleWatchlistUseCase(coinId) }
    }
}
