package com.vela.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val type: String,
    val direction: String,
    val targetValue: Double,
    val isTriggered: Boolean = false,
    val lastOhlcCheckTimestamp: Long = 0L,
    val ohlcAnchorTimestamp: Long = 0L
)
