package com.vela.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vela.data.source.local.dao.HomeAssetDao
import com.vela.data.source.local.model.AssetEntity

@Database(
    entities = [AssetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): HomeAssetDao
}