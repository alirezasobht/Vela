package com.vela.data.pricestore

import com.vela.domain.model.SimplePrice
import com.vela.domain.pricestore.SimplePriceStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class SimplePriceStoreImpl @Inject constructor() : SimplePriceStore {
    private val prices = MutableStateFlow<Map<String, SimplePrice>>(emptyMap())

    override fun observePrice(id: String): Flow<SimplePrice?> = prices
        .map { it[id] }
        .distinctUntilChanged()

    override suspend fun upsert(prices: Map<String, SimplePrice>) {
        this.prices.update { current ->
            current + prices.mapValues { (id, incoming) -> current[id]?.mergedWith(incoming) ?: incoming }
        }
    }

    // Field-by-field merge for partial update
    private fun SimplePrice.mergedWith(incoming: SimplePrice): SimplePrice = SimplePrice(
        price = incoming.price ?: price,
        priceChange = incoming.priceChange ?: priceChange,
        marketCap = incoming.marketCap ?: marketCap,
        totalVolume = incoming.totalVolume ?: totalVolume,
        priceChange24hAbsolute = incoming.priceChange24hAbsolute ?: priceChange24hAbsolute
    )
}
