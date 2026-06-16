package com.vela.ui.base.pricepolling

import com.vela.ui.common.components.model.SimplePriceUiModel
import kotlinx.coroutines.flow.Flow

interface PricePolling {
    fun onScreenVisible(visible: Boolean)

    fun observePrice(id: String): Flow<SimplePriceUiModel?>
}
