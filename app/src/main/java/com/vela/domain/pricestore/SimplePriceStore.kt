package com.vela.domain.pricestore

import com.vela.domain.model.SimplePrice
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the latest known price of a coin, regardless of which
 * endpoint or screen last fetched it. Repositories write into this store right after
 * a successful fetch; screens only ever read from it via [observePrice].
 */
interface SimplePriceStore {
    fun getPrices(): Map<String, SimplePrice>

    fun observePrice(id: String): Flow<SimplePrice?>

    suspend fun upsert(prices: Map<String, SimplePrice>)
}
