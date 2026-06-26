package com.vela.ui.screens.alerts

data class AlertGroupUiModel(
    val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val coinImage: String?,
    val initialPrice: String = "",
    val alerts: List<AlertRowUiModel>
)
