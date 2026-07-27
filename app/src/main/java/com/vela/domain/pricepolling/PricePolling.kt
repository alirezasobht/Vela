package com.vela.domain.pricepolling

import com.vela.domain.model.AppError
import kotlinx.coroutines.flow.SharedFlow

interface PricePolling {
    fun onScreenVisible(visible: Boolean)

    fun register(ids: Set<String>)

    fun observeError(): SharedFlow<AppError?>
}
