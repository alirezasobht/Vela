package com.vela.ui.screens.alerts

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.R
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.ui.common.util.LocalAnimatedVisibilityScope
import com.vela.ui.common.util.LocalSharedTransitionScope
import com.vela.ui.common.util.SharedTransitionKeys
import com.vela.ui.common.util.SharedTransitionWrapper

// ----- Actions -----

internal data class AlertFormActions(
    val onTypeSelected: (AlertType) -> Unit,
    val onDirectionSelected: (AlertDirection) -> Unit,
    val onValueChanged: (String) -> Unit,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit,
    val onDelete: () -> Unit = {}
)

// ----- Route -----

@Composable
fun AlertFormRoute(
    coinId: String,
    alertId: Long?,
    initialLabel: String?,
    formSessionId: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<AlertFormViewModel, AlertFormViewModel.Factory>(
        key = formSessionId.toString()
    ) { factory ->
        factory.create(coinId = coinId, alertId = alertId, initialLabel = initialLabel)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dismissEvent.collect { onDismiss() }
    }

    AlertFormContent(
        uiState = uiState,
        alertId = alertId,
        actions = AlertFormActions(
            onTypeSelected = viewModel::onTypeSelected,
            onDirectionSelected = viewModel::onDirectionSelected,
            onValueChanged = viewModel::onValueChanged,
            onConfirm = viewModel::onConfirm,
            onCancel = onDismiss,
            onDelete = viewModel::onDelete
        ),
        modifier = modifier
    )
}

// ----- Screen -----

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun AlertFormContent(
    uiState: AlertFormUiState,
    alertId: Long?,
    actions: AlertFormActions,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.alertLabel != null -> Text(
                text = uiState.alertLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .then(
                        if (alertId != null && sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedBounds(
                                    rememberSharedContentState(key = SharedTransitionKeys.alertLabel(alertId)),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    enter = fadeIn(tween(300)),
                                    exit = fadeOut(tween(300)),
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            )
            uiState.formHint != null -> Text(
                text = uiState.formHint,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

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

        if (alertId != null) {
            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = actions.onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.action_delete_alert),
                    color = MaterialTheme.colorScheme.error
                )
            }
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
private fun AlertFormContentPreview(
    uiState: AlertFormUiState,
    alertId: Long? = null
) {
    SharedTransitionWrapper {
        AlertFormContent(
            uiState = uiState,
            alertId = alertId,
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
    uiState = AlertFormUiState(
        editBtnResId = R.string.action_create_alert,
        formHint = "Select a type"
    )
)

@Preview(showBackground = true)
@Composable
private fun AlertFormContentEnabledPreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "80000",
        isSubmitEnabled = true,
        editBtnResId = R.string.action_create_alert,
        isValueInputEnabled = true,
        alertLabel = "Price above \$80,000"
    )
)

@Preview(showBackground = true)
@Composable
private fun AlertFormContentEditModePreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "80000",
        isSubmitEnabled = true,
        editBtnResId = R.string.action_edit_alert,
        isValueInputEnabled = true,
        alertLabel = "Price above \$70,000"
    ),
    alertId = 1L
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
        isValueInputEnabled = true,
        alertLabel = "Price above \$70,000"
    )
)

@Preview(showBackground = true)
@Composable
private fun AlertFormContentHintPreview() = AlertFormContentPreview(
    uiState = AlertFormUiState(
        type = AlertType.PRICE,
        direction = AlertDirection.ABOVE,
        value = "50000",
        isSubmitEnabled = false,
        editBtnResId = R.string.action_create_alert,
        isValueInputEnabled = true,
        formHint = "Target must be above current price"
    )
)
