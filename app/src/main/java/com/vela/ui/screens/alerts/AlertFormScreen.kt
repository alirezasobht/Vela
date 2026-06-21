package com.vela.ui.screens.alerts

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.R
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.ui.theme.VelaTheme

// ----- Actions -----

internal data class AlertFormActions(
    val onTypeSelected: (AlertType) -> Unit,
    val onDirectionSelected: (AlertDirection) -> Unit,
    val onValueChanged: (String) -> Unit,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
)

// ----- Route -----

@Composable
fun AlertFormRoute(
    coinId: String,
    alertId: Long?,
    formSessionId: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<AlertFormViewModel, AlertFormViewModel.Factory>(
        key = formSessionId.toString()
    ) { factory ->
        factory.create(coinId = coinId, alertId = alertId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dismissEvent.collect { onDismiss() }
    }

    val actions = AlertFormActions(
        onTypeSelected = viewModel::onTypeSelected,
        onDirectionSelected = viewModel::onDirectionSelected,
        onValueChanged = viewModel::onValueChanged,
        onConfirm = viewModel::onConfirm,
        onCancel = onDismiss
    )

    AlertFormContent(
        uiState = uiState,
        actions = actions,
        modifier = modifier
    )
}

// ----- Screen -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlertFormContent(
    uiState: AlertFormUiState,
    actions: AlertFormActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        TypeSelector(
            selectedType = uiState.type,
            onTypeSelected = actions.onTypeSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        DirectionSelector(
            selectedDirection = uiState.direction,
            onDirectionSelected = actions.onDirectionSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        ValueInput(
            value = uiState.value,
            onValueChange = actions.onValueChanged,
            type = uiState.type,
            enabled = uiState.isValueInputEnabled
        )

        Spacer(modifier = Modifier.height(24.dp))

        ConfirmButton(
            editBtnResId = uiState.editBtnResId,
            enabled = uiState.isSubmitEnabled,
            isLoading = uiState.isLoading,
            onClick = actions.onConfirm
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = actions.onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.action_cancel))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ----- Private composables -----

@Composable
private fun TypeSelector(
    selectedType: AlertType?,
    onTypeSelected: (AlertType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AlertType.entries.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = when (type) {
                            AlertType.PRICE -> stringResource(R.string.label_alert_type_price)
                            AlertType.PERCENT -> stringResource(R.string.label_alert_type_percent)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun DirectionSelector(
    selectedDirection: AlertDirection?,
    onDirectionSelected: (AlertDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AlertDirection.entries.forEach { direction ->
            FilterChip(
                selected = selectedDirection == direction,
                onClick = { onDirectionSelected(direction) },
                label = {
                    Text(
                        text = when (direction) {
                            AlertDirection.ABOVE -> stringResource(R.string.label_alert_direction_above)
                            AlertDirection.BELOW -> stringResource(R.string.label_alert_direction_below)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun ValueInput(
    value: String,
    onValueChange: (String) -> Unit,
    type: AlertType?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        prefix = if (type == AlertType.PRICE) {
            { Text(text = "$") }
        } else {
            null
        },
        suffix = if (type == AlertType.PERCENT) {
            { Text(text = "%") }
        } else {
            null
        },
        placeholder = {
            Text(
                text = when (type) {
                    AlertType.PERCENT -> stringResource(R.string.hint_alert_percent_value)
                    else -> stringResource(R.string.hint_alert_price_value)
                }
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}

@Composable
private fun ConfirmButton(
    @StringRes editBtnResId: Int,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(editBtnResId))
            }
        }
    }
}

// ----- Previews -----

@Composable
private fun AlertFormContentPreview(uiState: AlertFormUiState) {
    VelaTheme {
        AlertFormContent(
            uiState = uiState,
            actions = AlertFormActions(
                onTypeSelected = {},
                onDirectionSelected = {},
                onValueChanged = {},
                onConfirm = {},
                onCancel = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertFormContentEmptyPreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(editBtnResId = R.string.action_create_alert)
)

@Preview(showBackground = true)
@Composable
private fun AlertFormContentEnabledPreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "70000",
        isSubmitEnabled = true,
        editBtnResId = R.string.action_create_alert,
        isValueInputEnabled = true
    )
)

@Preview(showBackground = true)
@Composable
private fun AlertFormContentPercentPreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(
        type = AlertType.PERCENT,
        direction = AlertDirection.ABOVE,
        value = "5",
        isSubmitEnabled = true,
        editBtnResId = R.string.action_create_alert,
        isValueInputEnabled = true
    )
)

@Preview(showBackground = true)
@Composable
private fun AlertFormContentEditModePreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "70000",
        isSubmitEnabled = true,
        editBtnResId = R.string.action_edit_alert,
        isValueInputEnabled = true
    )
)

@Preview(showBackground = true)
@Composable
private fun AlertFormContentLoadingPreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "70000",
        isSubmitEnabled = true,
        isLoading = true,
        editBtnResId = R.string.action_create_alert,
        isValueInputEnabled = true
    )
)
