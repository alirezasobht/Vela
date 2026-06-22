package com.vela.ui.screens.detail.state

import com.vela.domain.model.TimeRange

data class DetailScreenState(
    val coinId: String = "",
    val selectedRange: TimeRange = TimeRange.ONE_DAY,
    val isChartFullScreen: Boolean = false,
    val isWatchlisted: Boolean = false,
    val alertFormState: AlertFormState = AlertFormState.Invisible,
    val content: DetailContentState = DetailContentState.Loading
)
