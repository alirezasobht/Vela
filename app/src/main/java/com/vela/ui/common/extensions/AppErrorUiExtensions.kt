package com.vela.ui.common.extensions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.vela.R
import com.vela.domain.model.AppError

@Composable
fun AppError.toMessage(): String = when (this) {
    is AppError.NoInternet -> stringResource(R.string.error_no_internet)
    is AppError.ServerError -> stringResource(R.string.error_server)
    is AppError.Unknown -> stringResource(R.string.error_unknown)
}

fun AppError.toIcon(): ImageVector = when (this) {
    is AppError.NoInternet -> Icons.Default.WifiOff
    is AppError.ServerError -> Icons.Default.CloudOff
    is AppError.Unknown -> Icons.Default.ErrorOutline
}