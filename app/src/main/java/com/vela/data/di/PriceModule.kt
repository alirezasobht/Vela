package com.vela.data.di

import com.vela.data.pricepolling.PricePollingConfigImpl
import com.vela.data.pricepolling.PricePollingImpl
import com.vela.data.pricepolling.PricePollingScope
import com.vela.data.pricestore.SimplePriceStoreImpl
import com.vela.domain.pricepolling.PricePolling
import com.vela.domain.pricepolling.PricePollingConfig
import com.vela.domain.pricestore.SimplePriceStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
abstract class PriceModule {
    @Binds
    @Singleton
    abstract fun bindSimplePriceStore(impl: SimplePriceStoreImpl): SimplePriceStore

    @Binds
    @Singleton
    abstract fun bindPricePolling(impl: PricePollingImpl): PricePolling

    @Binds
    @Singleton
    abstract fun bindPricePollingConfig(impl: PricePollingConfigImpl): PricePollingConfig

    companion object {
        @Provides
        @Singleton
        @PricePollingScope
        fun providePricePollingScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
