package com.vela.domain.repository

import com.vela.domain.model.CoinDetail
import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint

interface DetailRepository {
    suspend fun getCoinDetail(id: String): DataResult<CoinDetail>

    suspend fun getOhlc(
        id: String,
        days: Int
    ): DataResult<List<OhlcPoint>>
}
