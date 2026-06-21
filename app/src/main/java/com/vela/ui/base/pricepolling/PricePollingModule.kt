package com.vela.ui.base.pricepolling

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PricePollingModule {

    @Binds
    @Singleton
    abstract fun bindPricePollingConfig(impl: PricePollingConfigImpl): PricePollingConfig
}
