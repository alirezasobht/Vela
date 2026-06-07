package com.vela.ui.screens.detail.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vela.domain.model.TimeRange
import com.vela.ui.common.util.LandscapePreview
import com.vela.ui.theme.VelaTheme

@Composable
internal fun TimeRangeChips(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    if (isLandscape) {
        Column(
            modifier = modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
        ) {
            TimeRange.entries.forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    label = {
                        Text(
                            text = range.label,
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier
                )
            }
        }
    } else {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TimeRange.entries) { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    label = {
                        Text(
                            text = range.label,
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
    }
}

// ------ Previews --------

@Preview
@Composable
private fun TimeRangeChipsPreview() {
    VelaTheme {
        TimeRangeChips(
            selectedRange = TimeRange.ONE_DAY,
            onRangeSelected = {},
            isLandscape = false
        )
    }
}

@LandscapePreview
@Composable
private fun TimeRangeChipsLandscapePreview() {
    VelaTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            TimeRangeChips(
                selectedRange = TimeRange.ONE_DAY,
                onRangeSelected = {},
                isLandscape = true
            )
        }
    }
}
