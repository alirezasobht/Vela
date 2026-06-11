package com.vela.data.di

import com.vela.data.repository.AssetRepositoryImpl
import com.vela.data.repository.DetailRepositoryImpl
import com.vela.data.repository.MarketsRepositoryImpl
import com.vela.data.repository.PriceRepositoryImpl
import com.vela.data.repository.SearchRepositoryImpl
import com.vela.data.repository.WatchlistRepositoryImpl
import com.vela.domain.repository.AssetRepository
import com.vela.domain.repository.DetailRepository
import com.vela.domain.repository.MarketsRepository
import com.vela.domain.repository.PriceRepository
import com.vela.domain.repository.SearchRepository
import com.vela.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAssetRepository(impl: AssetRepositoryImpl): AssetRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindMarketsRepository(impl: MarketsRepositoryImpl): MarketsRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(impl: PriceRepositoryImpl): PriceRepository

    @Binds
    @Singleton
    abstract fun bindDetailRepository(impl: DetailRepositoryImpl): DetailRepository

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(impl: WatchlistRepositoryImpl): WatchlistRepository
}