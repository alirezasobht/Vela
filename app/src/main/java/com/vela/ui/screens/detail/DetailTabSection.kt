package com.vela.ui.screens.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.data.source.fake.FakeDetailDataSource
import com.vela.ui.screens.alerts.AlertRowUiModel
import com.vela.ui.screens.detail.state.CoinDetailUiModel
import com.vela.ui.screens.detail.state.DetailTab
import com.vela.ui.screens.detail.state.toUiModel
import com.vela.ui.theme.VelaTheme

@Composable
internal fun TabsSection(
    selectedTab: DetailTab,
    alerts: List<AlertRowUiModel>,
    detail: CoinDetailUiModel,
    marketCap: String,
    totalVolume: String,
    onEditAlert: (Long?) -> Unit,
) {
    when (selectedTab) {
        DetailTab.STATS ->
            StatsSection(
                marketCap = marketCap,
                totalVolume = totalVolume,
                circulatingSupply = detail.circulatingSupply,
                ath = detail.ath,
                atl = detail.atl,
                modifier = Modifier.padding(top = 8.dp)
            )

        DetailTab.ALERTS ->
            AlertsSection(
                alerts = alerts,
                onEditAlert = onEditAlert
            )
    }
}

// ------ previews -------

@Preview(showBackground = true)
@Composable
private fun TabsSectionStatsPreview() {
    val detail = FakeDetailDataSource.detail.toUiModel()
    VelaTheme {
        TabsSection(
            selectedTab = DetailTab.STATS,
            alerts = emptyList(),
            detail = detail,
            marketCap = detail.marketCap,
            totalVolume = detail.totalVolume,
            onEditAlert = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TabsSectionAlertsPreview() {
    val detail = FakeDetailDataSource.detail.toUiModel()
    VelaTheme {
        TabsSection(
            selectedTab = DetailTab.ALERTS,
            alerts = emptyList(),
            detail = detail,
            marketCap = detail.marketCap,
            totalVolume = detail.totalVolume,
            onEditAlert = {}
        )
    }
}
