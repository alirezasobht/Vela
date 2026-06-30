package com.vela.data.di

import com.vela.data.repository.AlertRepositoryImpl
import com.vela.data.repository.fake.FakeAssetRepository
import com.vela.data.repository.fake.FakeDetailRepository
import com.vela.data.repository.fake.FakeMarketsRepository
import com.vela.data.repository.fake.FakePriceRepository
import com.vela.data.repository.fake.FakeSearchRepository
import com.vela.data.repository.fake.FakeWatchlistRepository
import com.vela.domain.repository.AlertRepository
import com.vela.domain.repository.AssetRepository
import com.vela.domain.repository.DetailRepository
import com.vela.domain.repository.MarketsRepository
import com.vela.domain.repository.PriceRepository
import com.vela.domain.repository.SearchRepository
import com.vela.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class FakeRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAssetRepository(fake: FakeAssetRepository): AssetRepository

    @Binds
    @Singleton
    abstract fun bindDetailRepository(fake: FakeDetailRepository): DetailRepository

    @Binds
    @Singleton
    abstract fun bindMarketsRepository(impl: FakeMarketsRepository): MarketsRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(fake: FakeSearchRepository): SearchRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(fake: FakePriceRepository): PriceRepository

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(fake: FakeWatchlistRepository): WatchlistRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository
}
