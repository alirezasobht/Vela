package com.vela.domain.usecase

import com.vela.domain.model.CoinDetail
import com.vela.domain.model.DataResult
import com.vela.domain.repository.DetailRepository
import javax.inject.Inject

class GetCoinDetailUseCase
    @Inject
    constructor(
        private val repository: DetailRepository
    ) {
        suspend operator fun invoke(id: String): DataResult<CoinDetail> = repository.getCoinDetail(id)
    }
