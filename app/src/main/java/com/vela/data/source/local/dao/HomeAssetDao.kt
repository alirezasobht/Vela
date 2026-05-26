package com.vela.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.vela.data.source.local.model.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeAssetDao {

    @Query("SELECT * FROM assets ORDER BY marketCapRank ASC LIMIT :limit")
    fun observeAll(limit: Int): Flow<List<AssetEntity>>

    @Upsert
    suspend fun upsertAll(assets: List<AssetEntity>)

    @Query("DELETE FROM assets WHERE id NOT IN (:currentIds)")
    suspend fun deleteNotIn(currentIds: List<String>)

    @Transaction
    suspend fun refresh(assets: List<AssetEntity>) {
        upsertAll(assets)
        deleteNotIn(assets.map { it.id })
    }
}