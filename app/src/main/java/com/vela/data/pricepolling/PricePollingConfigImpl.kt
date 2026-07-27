package com.vela.data.pricepolling

import com.vela.domain.pricepolling.PricePollingConfig
import javax.inject.Inject

private const val DEFAULT_REFRESH_DELAY_SECONDS = 60 * 5L

class PricePollingConfigImpl @Inject constructor() : PricePollingConfig {
    override val refreshDelaySeconds: Long = DEFAULT_REFRESH_DELAY_SECONDS
}
