package com.vela.ui.screens.detail.state

import com.vela.domain.model.AppError
import com.vela.domain.model.OhlcPoint
import com.vela.ui.screens.alerts.AlertRowUiModel
import kotlinx.serialization.Serializable

@Serializable
enum class DetailTab { STATS, ALERTS }

sealed interface DetailContentState {
    data object Loading : DetailContentState

    data class Error(val appError: AppError) : DetailContentState

    data class Success(
        val detail: CoinDetailUiModel,
        val ohlcPoints: List<OhlcPoint> = emptyList(),
        val isChartLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val nonBlockingError: AppError? = null,
        val alerts: List<AlertRowUiModel> = emptyList(),
        val selectedTab: DetailTab = DetailTab.STATS
    ) : DetailContentState
}
