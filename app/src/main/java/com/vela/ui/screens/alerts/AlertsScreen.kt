package com.vela.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vela.R
import com.vela.ui.common.components.AlertRowItem
import com.vela.ui.common.components.FullScreenLoader
import com.vela.ui.common.components.ScreenHeader
import com.vela.ui.theme.VelaTheme

@Composable
fun AlertsRoute(
    navigateToDetail: (coinId: String, alertId: Long?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlertsScreen(
        uiState = uiState,
        onAlertClick = { coinId, alertId -> navigateToDetail(coinId, alertId) },
        modifier = modifier
    )
}

@Composable
internal fun AlertsScreen(
    uiState: AlertsUiState,
    onAlertClick: (coinId: String, alertId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.title_alerts))

        when (uiState) {
            is AlertsUiState.Loading -> FullScreenLoader()

            is AlertsUiState.Empty -> EmptyState()

            is AlertsUiState.Success ->
                AlertsList(
                    groups = uiState.groups,
                    onAlertClick = onAlertClick
                )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.label_alerts_empty_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.label_alerts_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AlertsList(
    groups: List<AlertGroupUiModel>,
    onAlertClick: (coinId: String, alertId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(groups, key = { it.coinId }) { group ->
            AlertGroup(
                group = group,
                onAlertClick = { alertId -> onAlertClick(group.coinId, alertId) }
            )
        }
    }
}

@Composable
private fun AlertGroup(
    group: AlertGroupUiModel,
    onAlertClick: (alertId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            HeaderRow(group = group, onClick = { onAlertClick(null) })
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )

        group.alerts.forEach { alert ->
            AlertRowItem(
                alert = alert,
                onClick = { onAlertClick(alert.id) }
            )
        }
    }
}

@Composable
private fun HeaderRow(
    group: AlertGroupUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = group.coinImage,
            contentDescription = group.coinName,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape),
            placeholder = rememberVectorPainter(Icons.Default.Paid),
            error = rememberVectorPainter(Icons.Default.MonetizationOn)
        )
        Text(
            text = "${group.coinName} \u00b7 ${group.coinSymbol.uppercase()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ----- Previews -----

@Composable
private fun AlertsScreenPreview(uiState: AlertsUiState) {
    VelaTheme {
        AlertsScreen(uiState = uiState, onAlertClick = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertsScreenLoadingPreview() = AlertsScreenPreview(AlertsUiState.Loading)

@Preview(showBackground = true)
@Composable
private fun AlertsScreenEmptyPreview() = AlertsScreenPreview(AlertsUiState.Empty)

@Preview(showBackground = true)
@Composable
private fun AlertsScreenSuccessPreview() = AlertsScreenPreview(
    AlertsUiState.Success(
        groups = listOf(
            AlertGroupUiModel(
                coinId = "bitcoin",
                coinName = "Bitcoin",
                coinSymbol = "btc",
                coinImage = null,
                alerts = listOf(
                    AlertRowUiModel(id = 1L, label = "Price above \$80,000", isTriggered = false),
                    AlertRowUiModel(id = 2L, label = "Price below \$60,000", isTriggered = true)
                )
            ),
            AlertGroupUiModel(
                coinId = "ethereum",
                coinName = "Ethereum",
                coinSymbol = "eth",
                coinImage = null,
                alerts = listOf(
                    AlertRowUiModel(id = 3L, label = "Price change above 5%", isTriggered = false)
                )
            )
        )
    )
)
