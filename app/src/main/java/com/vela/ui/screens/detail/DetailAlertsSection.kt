package com.vela.ui.screens.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.R
import com.vela.ui.common.components.AlertRowItem
import com.vela.ui.screens.alerts.AlertRowUiModel
import com.vela.ui.theme.VelaTheme

@Composable
internal fun AlertsSection(
    alerts: List<AlertRowUiModel>,
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
                AlertRowItem(
                    alert = alert,
                    onClick = { onEditAlert(alert.id) }
                )
            }
        }
        Button(
            onClick = { onEditAlert(null) },
            modifier = Modifier
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

@Preview(showBackground = true)
@Composable
private fun AlertsSectionPreview() {
    VelaTheme {
        AlertsSection(
            alerts = listOf(
                AlertRowUiModel(id = 1L, label = "Price above \$70,000", isTriggered = false),
                AlertRowUiModel(id = 2L, label = "Price change below 5%", isTriggered = true)
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
