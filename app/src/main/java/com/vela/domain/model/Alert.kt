package com.vela.domain.model

data class Alert(
    val id: Long = 0,
    val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val type: AlertType,
    val direction: AlertDirection,
    val targetValue: Double,
    val isTriggered: Boolean = false,
    val lastOhlcCheckTimestamp: Long = 0L,
    val ohlcAnchorTimestamp: Long = 0L
)
