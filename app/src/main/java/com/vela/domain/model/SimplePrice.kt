package com.vela.domain.model

data class SimplePrice(
    val price: Double?,
    val priceChange: Double?,
    val marketCap: Double?,
    val totalVolume: Double?,
    // simple/price only returns percent change, not absolute 24h change
    val priceChange24hAbsolute: Double? = price?.let { p -> priceChange?.let { c -> p * (c / 100.0) } }
)
