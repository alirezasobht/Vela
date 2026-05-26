package com.vela.data.di

import com.vela.data.repository.AssetRepositoryImpl
import com.vela.data.source.remote.api.CoinGeckoApi
import com.vela.domain.repository.AssetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAssetRepository(api: CoinGeckoApi): AssetRepository =
        AssetRepositoryImpl(api)
}