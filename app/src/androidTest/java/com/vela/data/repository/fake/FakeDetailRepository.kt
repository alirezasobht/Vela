package com.vela.data.repository.fake

import com.vela.data.source.fake.FakeDetailDataSource
import com.vela.data.source.fake.FakeOhlcDataSource
import com.vela.domain.model.CoinDetail
import com.vela.domain.model.DataResult
import com.vela.domain.model.OhlcPoint
import com.vela.domain.repository.DetailRepository
import javax.inject.Inject

class FakeDetailRepository @Inject constructor() : DetailRepository {
    override suspend fun getCoinDetail(coinId: String): DataResult<CoinDetail> =
        DataResult.Success(FakeDetailDataSource.detail)

    override suspend fun getOhlc(id: String, days: Int): DataResult<List<OhlcPoint>> =
        DataResult.Success(FakeOhlcDataSource.bitcoinOhlc)
}