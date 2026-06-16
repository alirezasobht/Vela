package com.vela.ui.base.pricepolling

import com.vela.domain.model.AppError

interface PricePollingDelegate {
    fun getIds(): List<String>

    fun onPriceError(error: AppError?)
}
