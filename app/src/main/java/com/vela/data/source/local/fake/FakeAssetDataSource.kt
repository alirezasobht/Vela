package com.vela.data.source.local.fake

import com.vela.domain.model.Asset

object FakeAssetDataSource {

    val assets = listOf(
        Asset(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "",
            currentPrice = 67420.0,
            priceChangePercent24h = 2.4,
            marketCapRank = 1,
            sparkline = listOf(65000.0, 65800.0, 66100.0, 65700.0, 66500.0, 67000.0, 67420.0)
        ),
        Asset(
            id = "ethereum",
            symbol = "eth",
            name = "Ethereum",
            image = "",
            currentPrice = null,
            priceChangePercent24h = null,
            marketCapRank = null,
            sparkline = null
        ),
        Asset(
            id = "tether",
            symbol = "usdt",
            name = "Tether",
            image = "",
            currentPrice = 1.0,
            priceChangePercent24h = 0.0,
            marketCapRank = 3,
            sparkline = listOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
        ),
        Asset(
            id = "solana",
            symbol = "sol",
            name = "Solana",
            image = "",
            currentPrice = 172.5,
            priceChangePercent24h = 5.8,
            marketCapRank = 5,
            sparkline = listOf(155.0, 158.0, 162.0, 160.0, 165.0, 170.0, 172.5)
        ),
        Asset(
            id = "ripple",
            symbol = "xrp",
            name = "XRP",
            image = "",
            currentPrice = 0.52,
            priceChangePercent24h = -0.8,
            marketCapRank = 6,
            sparkline = listOf(0.55, 0.54, 0.53, 0.54, 0.53, 0.52, 0.52)
        ),
        Asset(
            id = "cardano",
            symbol = "ada",
            name = "Cardano",
            image = "",
            currentPrice = 0.45,
            priceChangePercent24h = 1.2,
            marketCapRank = 9,
            sparkline = listOf(0.43, 0.43, 0.44, 0.44, 0.45, 0.45, 0.45)
        ),
        Asset(
            id = "avalanche-2",
            symbol = "avax",
            name = "Avalanche",
            image = "",
            currentPrice = 38.2,
            priceChangePercent24h = -0.7,
            marketCapRank = 11,
            sparkline = listOf(39.5, 39.0, 38.8, 38.5, 38.6, 38.3, 38.2)
        )
    )
}