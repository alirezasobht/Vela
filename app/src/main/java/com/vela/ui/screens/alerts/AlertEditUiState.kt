package com.vela.ui.screens.alerts

import androidx.annotation.StringRes
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType

data class AlertEditUiState(
    val type: AlertType? = null,
    val direction: AlertDirection? = null,
    val value: String = "",
    val isSubmitEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isValueInputEnabled: Boolean = false,
    @StringRes val editBtnResId: Int
)
