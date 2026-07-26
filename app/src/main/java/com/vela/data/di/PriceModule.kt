package com.vela.data.di

import com.vela.data.pricestore.SimplePriceStoreImpl
import com.vela.domain.pricestore.SimplePriceStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PriceModule {
    @Binds
    @Singleton
    abstract fun bindSimplePriceStore(impl: SimplePriceStoreImpl): SimplePriceStore
}
