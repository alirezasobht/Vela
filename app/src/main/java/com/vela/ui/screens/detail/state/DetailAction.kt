package com.vela.ui.screens.detail.state

import com.vela.domain.model.TimeRange

sealed class DetailAction {
    data object Back : DetailAction()
    data object Retry : DetailAction()
    data object ToggleFullScreen : DetailAction()
    data object ToggleWatchlist : DetailAction()
    data object DismissAlertForm : DetailAction()
    data class RangeSelected(val range: TimeRange) : DetailAction()
    data class TabSelected(val tab: DetailTab) : DetailAction()
    data class EditAlert(val id: Long?) : DetailAction()
}
