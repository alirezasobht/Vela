package com.vela.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vela.data.source.local.AppDatabase
import com.vela.data.source.local.dao.AlertDao
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

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE alerts ADD COLUMN lastOhlcCheckTimestamp INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE alerts ADD COLUMN ohlcAnchorTimestamp INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room
        .databaseBuilder(
            context,
            AppDatabase::class.java,
            "vela.db"
        )
        .fallbackToDestructiveMigration(false)
        .addMigrations(MIGRATION_3_4)
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
