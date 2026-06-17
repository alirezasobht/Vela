package com.vela.ui.screens.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vela.R
import com.vela.ui.screens.detail.state.DetailTab
import com.vela.ui.theme.VelaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailTabRow(
    selectedTab: DetailTab,
    onTabSelected: (DetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier
    ) {
        DetailTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text =
                            when (tab) {
                                DetailTab.STATS -> stringResource(R.string.label_tab_stats)
                                DetailTab.ALERTS -> stringResource(R.string.label_tab_alerts)
                            }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailTabRowPreview() {
    VelaTheme {
        DetailTabRow(
            selectedTab = DetailTab.STATS,
            onTabSelected = {}
        )
    }
}
