package com.vela.ui.base.pricepolling

import javax.inject.Inject

private const val DEFAULT_REFRESH_DELAY_SECONDS = 5 * 60L

class PricePollingConfig @Inject constructor() {
    val refreshDelaySeconds: Long = DEFAULT_REFRESH_DELAY_SECONDS
}
