package com.vela.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vela.data.source.local.dao.HomeAssetDao
import com.vela.data.source.local.dao.WatchlistDao
import com.vela.data.source.local.model.AssetEntity
import com.vela.data.source.local.model.WatchlistEntity

@Database(
    entities = [AssetEntity::class, WatchlistEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): HomeAssetDao

    abstract fun watchlistDao(): WatchlistDao
}
