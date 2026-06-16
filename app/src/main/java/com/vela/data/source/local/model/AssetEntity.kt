package com.vela.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val name: String,
    val image: String?,
    val currentPrice: Double?,
    val priceChangePercent24h: Double?,
    val marketCapRank: Int?,
    val sparkline: String?
)
