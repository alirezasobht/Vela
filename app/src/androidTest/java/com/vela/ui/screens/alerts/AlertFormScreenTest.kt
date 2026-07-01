package com.vela.ui.screens.alerts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.R
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import com.vela.ui.theme.VelaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertFormScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun defaultActions(
        onTypeSelected: (AlertType) -> Unit = {},
        onDirectionSelected: (AlertDirection) -> Unit = {},
        onValueChanged: (String) -> Unit = {},
        onConfirm: () -> Unit = {},
        onCancel: () -> Unit = {},
        onDelete: () -> Unit = {}
    ) = AlertFormActions(
        onTypeSelected = onTypeSelected,
        onDirectionSelected = onDirectionSelected,
        onValueChanged = onValueChanged,
        onConfirm = onConfirm,
        onCancel = onCancel,
        onDelete = onDelete
    )

    private fun setContent(
        uiState: AlertFormUiState,
        alertId: Long? = null,
        actions: AlertFormActions = defaultActions()
    ) {
        composeRule.setContent {
            VelaTheme {
                AlertFormContent(
                    uiState = uiState,
                    alertId = alertId,
                    actions = actions
                )
            }
        }
    }

    // ----- type/direction chips -----

    @Test
    fun emptyState_typeChipsVisible() {
        setContent(AlertFormUiState(editBtnResId = R.string.action_create_alert))
        composeRule.onNodeWithText("Price").assertIsDisplayed()
        composeRule.onNodeWithText("% Change").assertIsDisplayed()
    }

    @Test
    fun emptyState_directionChipsVisible() {
        setContent(AlertFormUiState(editBtnResId = R.string.action_create_alert))
        composeRule.onNodeWithText("Above").assertIsDisplayed()
        composeRule.onNodeWithText("Below").assertIsDisplayed()
    }

    @Test
    fun tapTypeChip_invokesOnTypeSelected() {
        var selected: AlertType? = null
        setContent(
            uiState = AlertFormUiState(editBtnResId = R.string.action_create_alert),
            actions = defaultActions(onTypeSelected = { selected = it })
        )
        composeRule.onNodeWithText("Price").performClick()
        assertEquals(AlertType.PRICE, selected)
    }

    @Test
    fun tapDirectionChip_invokesOnDirectionSelected() {
        var selected: AlertDirection? = null
        setContent(
            uiState = AlertFormUiState(editBtnResId = R.string.action_create_alert),
            actions = defaultActions(onDirectionSelected = { selected = it })
        )
        composeRule.onNodeWithText("Above").performClick()
        assertEquals(AlertDirection.ABOVE, selected)
    }

    // ----- value input -----

    @Test
    fun priceType_showsDollarPrefix() {
        setContent(
            AlertFormUiState(
                type = AlertType.PRICE,
                editBtnResId = R.string.action_create_alert,
                isValueInputEnabled = true
            )
        )
        composeRule.onNodeWithText("$").assertIsDisplayed()
    }

    @Test
    fun percentType_showsPercentSuffix() {
        setContent(
            AlertFormUiState(
                type = AlertType.PERCENT,
                editBtnResId = R.string.action_create_alert,
                isValueInputEnabled = true
            )
        )
        composeRule.onNodeWithText("%").assertIsDisplayed()
    }

    // ----- confirm button -----

    @Test
    fun submitDisabled_confirmButtonDisabled() {
        setContent(
            AlertFormUiState(
                isSubmitEnabled = false,
                editBtnResId = R.string.action_create_alert
            )
        )
        composeRule.onNodeWithText("Add Alert").assertIsNotEnabled()
    }

    @Test
    fun submitEnabled_confirmButtonEnabled() {
        setContent(
            AlertFormUiState(
                isSubmitEnabled = true,
                editBtnResId = R.string.action_create_alert
            )
        )
        composeRule.onNodeWithText("Add Alert").assertIsEnabled()
    }

    @Test
    fun editMode_confirmButtonShowsEditLabel() {
        setContent(
            AlertFormUiState(
                isSubmitEnabled = true,
                editBtnResId = R.string.action_edit_alert
            )
        )
        composeRule.onNodeWithText("Edit Alert").assertIsDisplayed()
    }

    @Test
    fun loadingState_showsSpinner() {
        setContent(
            AlertFormUiState(
                isSubmitEnabled = true,
                isLoading = true,
                editBtnResId = R.string.action_create_alert
            )
        )
        composeRule.onNodeWithText("Add Alert").assertDoesNotExist()
    }

    // ----- alert label -----

    @Test
    fun alertLabel_visibleWhenPresent() {
        setContent(
            AlertFormUiState(
                editBtnResId = R.string.action_create_alert,
                alertLabel = "Price above \$80,000"
            )
        )
        composeRule.onNodeWithText("Price above \$80,000").assertIsDisplayed()
    }

    @Test
    fun alertLabel_notVisibleWhenNull() {
        setContent(
            AlertFormUiState(
                editBtnResId = R.string.action_create_alert,
                alertLabel = null
            )
        )
        composeRule.onNodeWithText("Price above \$80,000").assertDoesNotExist()
    }

    // ----- form hint -----

    @Test
    fun formHint_visibleWhenAlertLabelIsNull() {
        setContent(
            AlertFormUiState(
                editBtnResId = R.string.action_create_alert,
                alertLabel = null,
                formHint = "Select a type"
            )
        )
        composeRule.onNodeWithText("Select a type").assertIsDisplayed()
    }

    @Test
    fun formHint_notVisibleWhenAlertLabelPresent() {
        setContent(
            AlertFormUiState(
                editBtnResId = R.string.action_create_alert,
                alertLabel = "Price above \$80,000",
                formHint = "Select a type"
            )
        )
        composeRule.onNodeWithText("Select a type").assertDoesNotExist()
        composeRule.onNodeWithText("Price above \$80,000").assertIsDisplayed()
    }

    // ----- cancel button -----

    @Test
    fun cancelButton_invokesOnCancel() {
        var cancelled = false
        setContent(
            uiState = AlertFormUiState(editBtnResId = R.string.action_create_alert),
            actions = defaultActions(onCancel = { cancelled = true })
        )
        composeRule.onNodeWithText("Cancel").performClick()
        assertEquals(true, cancelled)
    }

    // ----- delete button -----

    @Test
    fun deleteButton_visibleInEditMode() {
        setContent(
            uiState = AlertFormUiState(editBtnResId = R.string.action_edit_alert),
            alertId = 1L
        )
        composeRule.onNodeWithText("Delete Alert").assertIsDisplayed()
    }

    @Test
    fun deleteButton_notVisibleInCreateMode() {
        setContent(
            uiState = AlertFormUiState(editBtnResId = R.string.action_create_alert),
            alertId = null
        )
        composeRule.onNodeWithText("Delete Alert").assertDoesNotExist()
    }

    @Test
    fun deleteButton_invokesOnDelete() {
        var deleted = false
        setContent(
            uiState = AlertFormUiState(editBtnResId = R.string.action_edit_alert),
            alertId = 1L,
            actions = defaultActions(onDelete = { deleted = true })
        )
        composeRule.onNodeWithText("Delete Alert").performClick()
        assertEquals(true, deleted)
    }
}
