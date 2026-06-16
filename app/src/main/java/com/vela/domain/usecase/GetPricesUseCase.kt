package com.vela.domain.usecase

import com.vela.domain.model.DataResult
import com.vela.domain.model.SimplePrice

fun interface GetPricesUseCase {
    suspend operator fun invoke(ids: List<String>): DataResult<Map<String, SimplePrice>>
}
