package com.vela.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import com.vela.ui.theme.VelaTheme

@Composable
internal fun StatsSection(
    marketCap: String,
    totalVolume: String,
    circulatingSupply: String,
    ath: String,
    atl: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        StatRow(label = stringResource(R.string.label_market_cap), value = marketCap)
        StatRow(label = stringResource(R.string.label_24h_volume), value = totalVolume)
        StatRow(label = stringResource(R.string.label_circulating_supply), value = circulatingSupply)
        StatRow(label = stringResource(R.string.label_ath), value = ath)
        StatRow(label = stringResource(R.string.label_atl), value = atl)
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsSectionPreview() {
    VelaTheme {
        StatsSection(
            marketCap = "$1,320,000,000,000",
            totalVolume = "$38,400,000,000",
            circulatingSupply = "19,700,000 BTC",
            ath = "$73,750",
            atl = "$67.81"
        )
    }
}
