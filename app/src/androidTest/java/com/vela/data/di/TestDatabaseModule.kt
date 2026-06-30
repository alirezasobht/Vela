package com.vela.data.di

import android.content.Context
import androidx.room.Room
import com.vela.data.source.local.AppDatabase
import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.dao.HomeAssetDao
import com.vela.data.source.local.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    @Provides
    @Singleton
    fun provideHomeAssetDao(database: AppDatabase): HomeAssetDao = database.assetDao()

    @Provides
    @Singleton
    fun provideWatchlistDao(database: AppDatabase): WatchlistDao = database.watchlistDao()

    @Provides
    @Singleton
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()
}
