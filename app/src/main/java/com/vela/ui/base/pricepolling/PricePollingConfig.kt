package com.vela.ui.base.pricepolling

import javax.inject.Inject

private const val DEFAULT_REFRESH_DELAY_SECONDS = 5 * 60L

interface PricePollingConfig {
    val refreshDelaySeconds: Long
}

class PricePollingConfigImpl @Inject constructor() : PricePollingConfig {
    override val refreshDelaySeconds: Long = DEFAULT_REFRESH_DELAY_SECONDS
}
