package com.vela.ui.screens.detail.state

import com.vela.ui.screens.alerts.AlertRowUiModel

sealed class AlertFormState {
    data object Invisible : AlertFormState()

    data class Visible(
        val formSessionId: Int,
        val alertRowUiModel: AlertRowUiModel?
    ) : AlertFormState()
}
