package com.vela.data.source.fake

import com.vela.domain.model.CoinDetail

object FakeDetailDataSource {

    val detail = CoinDetail(
        id = "bitcoin",
        name = "Bitcoin",
        symbol = "btc",
        image = null,
        currentPrice = 67420.0,
        priceChange24h = 1240.0,
        priceChangePercent24h = 1.87,
        marketCap = 1_320_000_000_000.0,
        totalVolume = 38_400_000_000.0,
        circulatingSupply = 19_700_000.0,
        ath = 73750.0,
        atl = 67.81
    )
}