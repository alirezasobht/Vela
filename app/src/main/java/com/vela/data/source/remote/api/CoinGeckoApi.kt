package com.vela.data.source.remote.api

import com.vela.data.source.remote.model.CategoryDto
import com.vela.data.source.remote.model.CoinDetailDto
import com.vela.data.source.remote.model.CoinDto
import com.vela.data.source.remote.model.SearchResponseDto
import com.vela.data.source.remote.model.SimplePriceDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {

    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") limit: Int,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChangePercentage: String = "24h",
        @Query("category") category: String? = null
    ): List<CoinDto>

    @GET("coins/markets")
    suspend fun getMarketsByIds(
        @Query("vs_currency") currency: String = "usd",
        @Query("ids") ids: String,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChangePercentage: String = "24h"
    ): List<CoinDto>

    @GET("coins/markets")
    suspend fun getCoinDetail(
        @Query("vs_currency") currency: String = "usd",
        @Query("ids") id: String
    ): List<CoinDetailDto>

    @GET("coins/{id}/ohlc")
    suspend fun getOhlc(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: Int,
        @Query("precision") precision: Int = 5
    ): List<List<Double>>

    @GET("search")
    suspend fun search(
        @Query("query") query: String
    ): SearchResponseDto

    @GET("coins/categories")
    suspend fun getCategories(
        @Query("order") order: String = "market_cap_desc"
    ): List<CategoryDto>

    @GET("simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") currencies: String = "usd",
        @Query("include_24hr_change") include24hChange: Boolean = true,
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_24h_vol") includeVolume: Boolean = true
    ): Map<String, SimplePriceDto>
}