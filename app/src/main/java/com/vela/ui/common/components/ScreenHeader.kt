package com.vela.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.ui.theme.VelaTheme

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    controlsRow1: @Composable RowScope.() -> Unit = {},
    controlsRow2: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = controlsRow1
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = controlsRow2
        )
    }
}

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderTitleOnlyPreview() {
    VelaTheme {
        ScreenHeader(title = "Watchlist")
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderWithSubtitlePreview() {
    VelaTheme {
        ScreenHeader(
            title = "Markets",
            subtitle = "Monday, 25 May"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderWithRow1ControlsPreview() {
    VelaTheme {
        ScreenHeader(
            title = "Markets",
            subtitle = "Monday, 25 May",
            controlsRow1 = {
                FilterChip(selected = true, onClick = {}, label = { Text("50") })
                FilterChip(selected = false, onClick = {}, label = { Text("100") })
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderWithRow2ControlsPreview() {
    VelaTheme {
        ScreenHeader(
            title = "Markets",
            controlsRow2 = {
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text("All") },
                    leadingIcon = {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                    }
                )
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Market Cap") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderBothRowsPreview() {
    VelaTheme {
        ScreenHeader(
            title = "Markets",
            subtitle = "Monday, 25 May",
            controlsRow1 = {
                FilterChip(selected = true, onClick = {}, label = { Text("50") })
            },
            controlsRow2 = {
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text("All") },
                    leadingIcon = {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                    }
                )
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Market Cap") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                    }
                )
            }
        )
    }
}
