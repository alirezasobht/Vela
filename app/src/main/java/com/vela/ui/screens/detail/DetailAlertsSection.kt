package com.vela.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.R
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.ui.theme.VelaTheme

@Composable
internal fun AlertsSection(
    alerts: List<Alert>,
    onEditAlert: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (alerts.isEmpty()) {
            Text(
                text = stringResource(R.string.label_alerts_empty_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            alerts.forEach { alert ->
                AlertRow(
                    alert = alert,
                    onClick = { onEditAlert(alert.id) }
                )
            }
        }
        Button(
            onClick = { onEditAlert(null) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.action_create_alert),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun AlertRow(
    alert: Alert,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${alert.type.name} ${alert.direction.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = alert.targetValue.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (alert.isTriggered) {
                Text(
                    text = stringResource(R.string.label_alert_triggered),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ).padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertsSectionPreview() {
    VelaTheme {
        AlertsSection(
            alerts =
                listOf(
                    Alert(
                        id = 1,
                        coinId = "bitcoin",
                        coinName = "Bitcoin",
                        coinSymbol = "btc",
                        type = AlertType.PRICE,
                        direction = AlertDirection.ABOVE,
                        targetValue = 70000.0,
                        isTriggered = false
                    ),
                    Alert(
                        id = 2,
                        coinId = "bitcoin",
                        coinName = "Bitcoin",
                        coinSymbol = "btc",
                        type = AlertType.PERCENT,
                        direction = AlertDirection.BELOW,
                        targetValue = 5.0,
                        isTriggered = true
                    )
                ),
            onEditAlert = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertsSectionEmptyPreview() {
    VelaTheme {
        AlertsSection(
            alerts = emptyList(),
            onEditAlert = {}
        )
    }
}
