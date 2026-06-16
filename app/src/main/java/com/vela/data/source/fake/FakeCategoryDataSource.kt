package com.vela.data.source.fake

import com.vela.domain.model.MarketCategory

object FakeCategoryDataSource {
    val categories =
        listOf(
            MarketCategory.ALL,
            MarketCategory(id = "layer-1", displayName = "Layer 1"),
            MarketCategory(id = "decentralized-finance-defi", displayName = "DeFi"),
            MarketCategory(id = "meme-token", displayName = "Meme"),
            MarketCategory(id = "artificial-intelligence", displayName = "AI"),
            MarketCategory(id = "gaming", displayName = "Gaming"),
        )
}
