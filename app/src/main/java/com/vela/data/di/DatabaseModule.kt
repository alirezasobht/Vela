package com.vela.data.di

import android.content.Context
import androidx.room.Room
import com.vela.data.source.local.AppDatabase
import com.vela.data.source.local.dao.HomeAssetDao
import com.vela.data.source.local.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vela.db"
        )
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    @Singleton
    fun provideHomeAssetDao(database: AppDatabase): HomeAssetDao =
        database.assetDao()

    @Provides
    @Singleton
    fun provideWatchlistDao(database: AppDatabase): WatchlistDao =
        database.watchlistDao()
}